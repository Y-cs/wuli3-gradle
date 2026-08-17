package com.kjs.wuli3.rocket.internal;

import com.kjs.wuli3.core.error.code.CommonErrors;
import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.error.SendFailedException;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapper;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 RocketMQ Java Client v5 的远程事件传输实现。
 *
 * <p>应用负责创建并关闭 {@link Producer}；starter 只在选择 v5 客户端时注入该 Producer。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@RequiredArgsConstructor
public final class RocketV5RemoteEventTransport implements RemoteEventTransport<RocketPublishOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketV5RemoteEventTransport.class);

    @NonNull
    private final Producer producer;

    @NonNull
    private final ClientServiceProvider clientServiceProvider;

    @NonNull
    private final RocketMessageWrapperEncoder encoder;

    @NonNull
    private final Clock clock;

    @Override
    public Class<RocketPublishOptions> supportedOptionsType() {
        return RocketPublishOptions.class;
    }

    @Override
    public void send(final RocketPublishOptions options, final EventEnvelope<?>... envelopes) {
        Objects.requireNonNull(options, "options cannot be null");
        Objects.requireNonNull(envelopes, "envelopes cannot be null");
        for (final EventEnvelope<?> envelope : envelopes) {
            final RocketMessageWrapper wireMessage = this.encoder.encode(envelope, options);
            final Message message = this.createMessage(wireMessage, options);
            if (options.async()) {
                this.sendAsync(message, envelope);
                continue;
            }
            try {
                this.producer.send(message);
            } catch (final ClientException | RuntimeException exception) {
                throw new SendFailedException("RocketMQ event send failed", exception);
            }
        }
    }

    private Message createMessage(final RocketMessageWrapper wireMessage, final RocketPublishOptions options) {
        final String orderKey = wireMessage.orderKey();
        final Duration delay = wireMessage.delay();
        if (orderKey != null && delay != null) {
            throw new ErrorCodeException(
                    CommonErrors.UNSUPPORTED_OPERATION,
                    "RocketMQ Java Client does not support ordered delayed " + "messages");
        }
        if (orderKey != null && options.async()) {
            throw new ErrorCodeException(
                    CommonErrors.UNSUPPORTED_OPERATION, "RocketMQ Java Client does not support async FIFO messages");
        }

        final MessageBuilder builder = this.clientServiceProvider
                .newMessageBuilder()
                .setTopic(wireMessage.topic())
                .setBody(wireMessage.body())
                .setKeys(wireMessage.key())
                .setTag(wireMessage.tag());
        wireMessage.headers().forEach((key, value) -> builder.addProperty(key, String.valueOf(value)));
        if (orderKey != null) {
            builder.setMessageGroup(orderKey);
        }
        if (delay != null) {
            builder.setDeliveryTimestamp(Math.addExact(this.clock.millis(), delay.toMillis()));
        }
        return builder.build();
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void sendAsync(final Message message, final EventEnvelope<?> envelope) {
        try {
            this.producer.sendAsync(message).whenComplete((receipt, throwable) -> {
                if (throwable != null) {
                    RocketV5RemoteEventTransport.LOGGER.error(
                            "Async RocketMQ Java Client v5 event publication failed: topic={}, eventId={},"
                                    + " eventType={}",
                            envelope.topic(),
                            envelope.eventId(),
                            envelope.eventType(),
                            throwable);
                }
            });
        } catch (final RuntimeException exception) {
            throw new SendFailedException("RocketMQ async event send failed to start", exception);
        }
    }
}

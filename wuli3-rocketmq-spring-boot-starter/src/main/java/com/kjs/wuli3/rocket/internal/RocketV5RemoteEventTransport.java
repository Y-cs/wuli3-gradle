package com.kjs.wuli3.rocket.internal;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapper;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
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
 */
@RequiredArgsConstructor
public final class RocketV5RemoteEventTransport implements RemoteEventTransport {

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
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        final RocketMessageWrapper wireMessage = this.encoder.encode(envelope, options);
        final Message message = this.createMessage(wireMessage, options);
        if (options.isAsync()) {
            this.sendAsync(message, envelope);
            return;
        }
        try {
            this.producer.send(message);
        } catch (final ClientException | RuntimeException exception) {
            throw new EventTransport.SendFailedException("RocketMQ event send failed", exception);
        }
    }

    @Override
    public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        Objects.requireNonNull(envelopes, "envelopes cannot be null");
        for (final EventEnvelope<?> envelope : envelopes) {
            this.send(envelope, options);
        }
    }

    private Message createMessage(final RocketMessageWrapper wireMessage, final PublishOptions options) {
        final String orderKey = wireMessage.orderKey();
        final Duration delay = wireMessage.delay();
        if (orderKey != null && delay != null) {
            throw new ErrorCodeException(
                    SystemErrors.UNSUPPORTED_OPERATION,
                    "RocketMQ Java Client does not support ordered delayed " + "messages");
        }
        if (orderKey != null && options.isAsync()) {
            throw new ErrorCodeException(
                    SystemErrors.UNSUPPORTED_OPERATION, "RocketMQ Java Client does not support async FIFO messages");
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
        this.producer.sendAsync(message).whenComplete((receipt, throwable) -> {
            if (throwable != null) {
                RocketV5RemoteEventTransport.LOGGER.error(
                        "Async RocketMQ Java Client v5 event publication failed: topic={}, eventId={}, eventType={}",
                        envelope.topic(),
                        envelope.eventId(),
                        envelope.eventType(),
                        throwable);
            }
        });
    }
}

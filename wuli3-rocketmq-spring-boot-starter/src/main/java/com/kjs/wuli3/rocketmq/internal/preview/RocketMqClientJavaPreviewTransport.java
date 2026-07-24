package com.kjs.wuli3.rocketmq.internal.preview;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventMessageTransport;
import com.kjs.wuli3.event.EventMessageTransport.UnsupportedCapabilityException;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventMessageTransport;
import com.kjs.wuli3.rocketmq.internal.RocketMqEventMessageEncoder;
import com.kjs.wuli3.rocketmq.internal.RocketMqWireMessage;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import lombok.AccessLevel;
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
 * 仅供审查的 Java Client 传输实现；运行时启动和 Producer 生命周期仍由调用方负责。
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class RocketMqClientJavaPreviewTransport implements RemoteEventMessageTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqClientJavaPreviewTransport.class);

    @NonNull
    private final Producer producer;

    @NonNull
    private final ClientServiceProvider clientServiceProvider;

    @NonNull
    private final RocketMqEventMessageEncoder encoder;

    @NonNull
    private final Clock clock;

    @Override
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        final RocketMqWireMessage wireMessage = this.encoder.encode(envelope, options);
        final Message message = this.createMessage(wireMessage, options);
        if (options.isAsync()) {
            this.sendAsync(message, envelope);
            return;
        }
        try {
            this.producer.send(message);
        } catch (final ClientException exception) {
            throw new EventMessageTransport.SendFailedException("Failed to send RocketMQ event", exception);
        }
    }

    @Override
    public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        Objects.requireNonNull(envelopes, "envelopes cannot be null");
        for (final EventEnvelope<?> envelope : envelopes) {
            this.send(envelope, options);
        }
    }

    private Message createMessage(final RocketMqWireMessage wireMessage, final PublishOptions options) {
        final String orderKey = wireMessage.orderKey();
        final Duration delay = wireMessage.delay();
        if (orderKey != null && delay != null) {
            throw new UnsupportedCapabilityException("RocketMQ Java Client does not support ordered delayed messages");
        }
        if (orderKey != null && options.isAsync()) {
            throw new UnsupportedCapabilityException("RocketMQ Java Client does not support async FIFO messages");
        }

        final MessageBuilder builder = this.clientServiceProvider
                .newMessageBuilder()
                .setTopic(wireMessage.topic())
                .setBody(wireMessage.body())
                .setKeys(wireMessage.key())
                .setTag(wireMessage.tag());
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
                RocketMqClientJavaPreviewTransport.LOGGER.error(
                        "Async RocketMQ Java Client preview publication failed: topic={}, eventId={}, eventType={}",
                        envelope.topic(),
                        envelope.eventId(),
                        envelope.eventType(),
                        throwable);
            }
        });
    }
}

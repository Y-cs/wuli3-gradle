package com.kjs.wuli3.rocket.internal;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapper;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

/**
 * 基于 {@link RocketMQTemplate} 的默认尽力而为远程事件传输实现。
 */
public final class RocketRemoteEventTransport implements RemoteEventTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketRemoteEventTransport.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketMessageWrapperEncoder encoder;

    /**
     * 使用指定的 RocketMQ 模板和公共事件编码器创建传输实现。
     *
     * @param rocketMQTemplate Apache SDK 桥接模板
     * @param encoder          公共事件线协议编码器
     */
    public RocketRemoteEventTransport(
            final RocketMQTemplate rocketMQTemplate, final RocketMessageWrapperEncoder encoder) {
        this.rocketMQTemplate = Objects.requireNonNull(rocketMQTemplate, "rocketMQTemplate");
        this.encoder = Objects.requireNonNull(encoder, "encoder");
    }

    @Override
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        final RocketMessageWrapper wireMessage = this.encoder.encode(envelope, options);
        this.sendEncoded(wireMessage, envelope, options);
    }

    @Override
    public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        Objects.requireNonNull(envelopes, "envelopes");
        for (final EventEnvelope<?> envelope : envelopes) {
            this.send(envelope, options);
        }
    }

    private void sendEncoded(
            final RocketMessageWrapper wireMessage, final EventEnvelope<?> envelope, final PublishOptions options) {
        try {
            final Message<byte[]> message = MessageBuilder.withPayload(wireMessage.body())
                    .copyHeaders(wireMessage.headers())
                    .setHeader(RocketMQHeaders.KEYS, wireMessage.key())
                    .setHeader(RocketMQHeaders.TAGS, wireMessage.tag())
                    .build();
            final Duration delay = wireMessage.delay();
            if (delay != null) {
                this.rocketMQTemplate.syncSendDelayTimeMills(wireMessage.topic(), message, delay.toMillis());
            } else if (wireMessage.orderKey() != null && options.isAsync()) {
                this.rocketMQTemplate.asyncSendOrderly(
                        wireMessage.topic(),
                        message,
                        wireMessage.orderKey(),
                        new RocketRemoteEventTransportCallback(envelope));
            } else if (wireMessage.orderKey() != null) {
                this.rocketMQTemplate.syncSendOrderly(wireMessage.topic(), message, wireMessage.orderKey());
            } else if (options.isAsync()) {
                this.rocketMQTemplate.asyncSend(
                        wireMessage.topic(), message, new RocketRemoteEventTransportCallback(envelope));
            } else {
                this.rocketMQTemplate.syncSend(wireMessage.topic(), message);
            }
        } catch (final ErrorCodeException exception) {
            throw exception;
        } catch (final RuntimeException exception) {
            throw new EventTransport.SendFailedException("RocketMQ event send failed", exception);
        }
    }

    @RequiredArgsConstructor
    public static class RocketRemoteEventTransportCallback implements SendCallback {

        private final EventEnvelope<?> envelope;

        @Override
        public void onSuccess(SendResult sendResult) {}

        @Override
        public void onException(Throwable e) {
            RocketRemoteEventTransport.LOGGER.error(
                    "Async RocketMQ event publication failed: topic={}, eventId={}, eventType={}",
                    envelope.topic(),
                    envelope.eventId(),
                    envelope.eventType(),
                    e);
        }
    }
}

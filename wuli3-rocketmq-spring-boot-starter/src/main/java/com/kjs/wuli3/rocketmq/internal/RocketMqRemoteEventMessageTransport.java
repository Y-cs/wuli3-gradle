package com.kjs.wuli3.rocketmq.internal;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventMessageTransport;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/** 基于 {@link RocketMQTemplate} 的默认尽力而为远程事件传输实现。 */
public final class RocketMqRemoteEventMessageTransport implements RemoteEventMessageTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqRemoteEventMessageTransport.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketMqEventMessageEncoder encoder;

    /**
     * 使用指定的 RocketMQ 模板和公共事件编码器创建传输实现。
     *
     * @param rocketMQTemplate Apache SDK 桥接模板
     * @param encoder 公共事件线协议编码器
     */
    public RocketMqRemoteEventMessageTransport(
            final RocketMQTemplate rocketMQTemplate, final RocketMqEventMessageEncoder encoder) {
        this.rocketMQTemplate = Objects.requireNonNull(rocketMQTemplate, "rocketMQTemplate");
        this.encoder = Objects.requireNonNull(encoder, "encoder");
    }

    @Override
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        final RocketMqWireMessage wireMessage = this.encoder.encode(envelope, options);
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
            final RocketMqWireMessage wireMessage, final EventEnvelope<?> envelope, final PublishOptions options) {
        final Message<byte[]> message = MessageBuilder.withPayload(wireMessage.body())
                .setHeader(RocketMQHeaders.KEYS, wireMessage.key())
                .setHeader(RocketMQHeaders.TAGS, wireMessage.tag())
                .build();
        final Duration delay = wireMessage.delay();
        if (delay != null) {
            this.rocketMQTemplate.syncSendDelayTimeMills(wireMessage.topic(), message, delay.toMillis());
        } else if (wireMessage.orderKey() != null && options.isAsync()) {
            this.rocketMQTemplate.asyncSendOrderly(
                    wireMessage.topic(), message, wireMessage.orderKey(), this.callback(envelope));
        } else if (wireMessage.orderKey() != null) {
            this.rocketMQTemplate.syncSendOrderly(wireMessage.topic(), message, wireMessage.orderKey());
        } else if (options.isAsync()) {
            this.rocketMQTemplate.asyncSend(wireMessage.topic(), message, this.callback(envelope));
        } else {
            this.rocketMQTemplate.syncSend(wireMessage.topic(), message);
        }
    }

    private SendCallback callback(final EventEnvelope<?> envelope) {
        return new SendCallback() {
            @Override
            public void onSuccess(final SendResult sendResult) {
                // 成功提交后无需执行额外的本地操作。
            }

            @Override
            public void onException(final Throwable throwable) {
                RocketMqRemoteEventMessageTransport.LOGGER.error(
                        "Async RocketMQ event publication failed: topic={}, eventId={}, eventType={}",
                        envelope.topic(),
                        envelope.eventId(),
                        envelope.eventType(),
                        throwable);
            }
        };
    }
}

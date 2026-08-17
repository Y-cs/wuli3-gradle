package com.kjs.wuli3.rabbit.internal;

import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.error.SendFailedException;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.TaskExecutor;

/** 基于 {@link RabbitTemplate} 的默认尽力而为远程事件传输实现。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class RabbitRemoteEventTransport implements RemoteEventTransport<RabbitPublishOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitRemoteEventTransport.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessageEncoder encoder;
    private final TaskExecutor applicationExecutor;

    /**
     * 使用指定的 RabbitTemplate 和编码器创建传输实现。
     *
     * @param rabbitTemplate      Spring AMQP 模板
     * @param encoder             公共事件线协议编码器
     * @param applicationExecutor 异步发送执行器
     */
    public RabbitRemoteEventTransport(
            final RabbitTemplate rabbitTemplate,
            final RabbitMessageEncoder encoder,
            final TaskExecutor applicationExecutor) {
        this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate, "rabbitTemplate");
        this.encoder = Objects.requireNonNull(encoder, "encoder");
        this.applicationExecutor = Objects.requireNonNull(applicationExecutor, "applicationExecutor");
    }

    @Override
    public Class<RabbitPublishOptions> supportedOptionsType() {
        return RabbitPublishOptions.class;
    }

    @Override
    public void send(final RabbitPublishOptions options, final EventEnvelope<?>... envelopes) {
        final RabbitPublishOptions actualOptions = Objects.requireNonNull(options, "options");
        final EventEnvelope<?>[] actualEnvelopes = Objects.requireNonNull(envelopes, "envelopes");
        for (final EventEnvelope<?> envelope : actualEnvelopes) {
            final Message message = this.encoder.encode(envelope);
            if (actualOptions.async()) {
                this.sendAsynchronously(envelope, message);
            } else {
                this.sendSynchronously(envelope, message);
            }
        }
    }

    private void sendSynchronously(final EventEnvelope<?> envelope, final Message message) {
        try {
            this.rabbitTemplate.send(envelope.topic(), envelope.eventType(), message);
        } catch (final ErrorCodeException exception) {
            throw exception;
        } catch (final RuntimeException exception) {
            throw new SendFailedException("RabbitMQ event send failed", exception);
        }
    }

    private void sendAsynchronously(final EventEnvelope<?> envelope, final Message message) {
        try {
            this.applicationExecutor.execute(() -> {
                try {
                    this.rabbitTemplate.send(envelope.topic(), envelope.eventType(), message);
                } catch (final RuntimeException exception) {
                    RabbitRemoteEventTransport.LOGGER.error(
                            "Async RabbitMQ event publication failed: exchange={}, routingKey={}, eventId={}, eventType={}",
                            envelope.topic(),
                            envelope.eventType(),
                            envelope.eventId(),
                            envelope.eventType(),
                            exception);
                }
            });
        } catch (final RuntimeException exception) {
            throw new SendFailedException("RabbitMQ async event send failed to start", exception);
        }
    }
}

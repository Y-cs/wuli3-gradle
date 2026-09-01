package com.kjs.wuli3.rabbit.internal;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/** 将公共事件线协议编码为 AMQP 消息。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class RabbitMessageEncoder {

    private final @Nullable ContextReader contextReader;
    private final ContextPropagator contextPropagator;

    /**
     * 使用当前上下文重建保留传播头信息的编码器。
     *
     * @param contextReader  可选的当前上下文读取器
     * @param contextPropagator 上下文字段编码器
     */
    public RabbitMessageEncoder(
            final @Nullable ContextReader contextReader, final ContextPropagator contextPropagator) {
        this.contextReader = contextReader;
        this.contextPropagator = Objects.requireNonNull(contextPropagator, "contextEncoder");
    }

    /**
     * 将事件序列化为 JSON body，并将传播上下文写入 AMQP headers。
     *
     * @param envelope 待序列化事件
     * @return AMQP 消息
     */
    public Message encode(final EventEnvelope<?> envelope) {
        final EventEnvelope<?> actualEnvelope = Objects.requireNonNull(envelope, "envelope");
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setMessageId(actualEnvelope.eventId());
        messageProperties.setType(actualEnvelope.eventType());
        this.writePropagationHeaders(messageProperties);
        return new Message(Jsons.toJsonBytes(actualEnvelope), messageProperties);
    }

    private void writePropagationHeaders(final MessageProperties messageProperties) {
        if (this.contextReader == null) {
            return;
        }
        final ContextSnapshot snapshot = this.contextReader.capture();
        this.contextPropagator.inject(snapshot, messageProperties::setHeader);
    }
}

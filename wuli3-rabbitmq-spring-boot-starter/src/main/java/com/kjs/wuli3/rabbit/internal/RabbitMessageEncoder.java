package com.kjs.wuli3.rabbit.internal;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/** 将公共事件线协议编码为 AMQP 消息。 */
public final class RabbitMessageEncoder {

    private final @Nullable ContextReader contextReader;
    private final ContextEncoder contextEncoder;

    /**
     * 使用当前上下文重建保留传播头信息的编码器。
     *
     * @param contextReader  可选的当前上下文读取器
     * @param contextEncoder 上下文字段编码器
     */
    public RabbitMessageEncoder(final @Nullable ContextReader contextReader, final ContextEncoder contextEncoder) {
        this.contextReader = contextReader;
        this.contextEncoder = Objects.requireNonNull(contextEncoder, "contextEncoder");
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
        this.contextEncoder.writeTo(snapshot, messageProperties::setHeader);
    }
}

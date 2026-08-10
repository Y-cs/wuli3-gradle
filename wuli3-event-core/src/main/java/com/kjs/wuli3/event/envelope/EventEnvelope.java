package com.kjs.wuli3.event.envelope;

import java.time.Instant;
import java.util.Objects;

/**
 * 不可变且与具体传输实现无关的事件元数据和载荷。
 *
 * @param <T>        载荷类型
 * @param topic      逻辑远程目标主题
 * @param eventType  稳定的事件契约名称
 * @param eventId    唯一事件标识
 * @param occurredOn 事件创建时间
 * @param payload    事件载荷
 */
public record EventEnvelope<T>(String topic, String eventType, String eventId, Instant occurredOn, T payload) {

    /**
     * 创建事件信封。
     */
    public EventEnvelope {
        topic = EventEnvelope.requireNonBlank(topic, "topic");
        eventType = EventEnvelope.requireNonBlank(eventType, "eventType");
        eventId = EventEnvelope.requireNonBlank(eventId, "eventId");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
        Objects.requireNonNull(payload, "payload cannot be null");
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name + " cannot be null").isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}

package com.kjs.wuli3.event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 不可变且与具体传输实现无关的事件元数据和载荷。
 *
 * @param <T> 载荷类型
 * @param headers 以浅不可变映射保存的传输元数据
 * @param topic 逻辑远程目标主题
 * @param eventType 稳定的事件契约名称
 * @param eventId 唯一事件标识
 * @param occurredOn 事件创建时间
 * @param payload 事件载荷
 */
public record EventEnvelope<T>(
        Map<String, Object> headers, String topic, String eventType, String eventId, Instant occurredOn, T payload) {

    /**
     * 创建事件信封，并对头信息做浅不可变快照。
     */
    public EventEnvelope {
        headers = Map.copyOf(Objects.requireNonNull(headers, "headers cannot be null"));
        topic = EventEnvelope.requireNonBlank(topic, "topic");
        eventType = EventEnvelope.requireNonBlank(eventType, "eventType");
        eventId = EventEnvelope.requireNonBlank(eventId, "eventId");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
        Objects.requireNonNull(payload, "payload cannot be null");
    }

    /**
     * 创建不包含头信息的事件信封。
     *
     * @param topic 逻辑远程目标主题
     * @param eventType 稳定的事件契约名称
     * @param eventId 唯一事件标识
     * @param occurredOn 事件创建时间
     * @param payload 事件载荷
     */
    public EventEnvelope(
            final String topic,
            final String eventType,
            final String eventId,
            final Instant occurredOn,
            final T payload) {
        this(Map.of(), topic, eventType, eventId, occurredOn, payload);
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name + " cannot be null").isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }

    public EventEnvelope<T> withHeader(final String key, final Object value) {
        final Map<String, Object> merged = new HashMap<>(headers);
        merged.put(key, value);
        return new EventEnvelope<>(merged, topic, eventType, eventId, occurredOn, payload);
    }
}

package com.kjs.wuli3.event.envelope;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** 为固定主题和事件类型创建标识一致的事件信封。 */
public final class EventEnvelopeTemplate {

    private final String topic;
    private final String eventType;

    private final Supplier<String> eventIdSupplier;

    private EventEnvelopeTemplate(final String topic, final String eventType, final Supplier<String> eventIdSupplier) {
        this.topic = EventEnvelopeTemplate.requireNonBlank(topic, "topic");
        this.eventType = EventEnvelopeTemplate.requireNonBlank(eventType, "eventType");
        this.eventIdSupplier = Objects.requireNonNull(eventIdSupplier, "eventIdSupplier cannot be null");
    }

    /**
     * 使用指定的事件标识生成器创建模板。
     *
     * @param topic 逻辑远程目标主题
     * @param eventType 稳定的事件契约名称
     * @param eventIdSupplier 唯一事件标识的来源
     * @return 可复用的事件信封模板
     */
    public static EventEnvelopeTemplate of(
            final String topic, final String eventType, final Supplier<String> eventIdSupplier) {
        return new EventEnvelopeTemplate(topic, eventType, eventIdSupplier);
    }

    /**
     * 创建使用 UUID 生成事件标识的模板。
     *
     * @param topic 逻辑远程目标主题
     * @param eventType 稳定的事件契约名称
     * @return 可复用的事件信封模板
     */
    public static EventEnvelopeTemplate of(final String topic, final String eventType) {
        return new EventEnvelopeTemplate(
                topic, eventType, () -> UUID.randomUUID().toString());
    }

    /**
     * 将载荷封装为新的事件信封。
     *
     * @param <T> 载荷类型
     * @param payload 事件载荷
     * @return 带有新生成标识和当前时间戳的事件信封
     */
    public <T> EventEnvelope<T> wrap(final T payload) {
        return new EventEnvelope<>(this.topic, this.eventType, this.eventIdSupplier.get(), Instant.now(), payload);
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name + " cannot be null").isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}

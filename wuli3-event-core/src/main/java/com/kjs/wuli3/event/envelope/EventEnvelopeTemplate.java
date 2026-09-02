package com.kjs.wuli3.event.envelope;

import com.kjs.wuli3.core.id.IdGenerator;
import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.core.time.ClockProvider;

import java.util.Objects;

/** 为固定主题和事件类型创建标识一致的事件信封。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class EventEnvelopeTemplate {

    private final String topic;
    private final String eventType;

    private final ClockProvider clockProvider;
    private final IdGenerator<String> idGenerator;

    private EventEnvelopeTemplate(final String topic, final String eventType, final ClockProvider clockProvider, final IdGenerator<String> idGenerator) {
        this.topic = EventEnvelopeTemplate.requireNonBlank(topic, "topic");
        this.eventType = EventEnvelopeTemplate.requireNonBlank(eventType, "eventType");
        this.clockProvider = Objects.requireNonNull(clockProvider, "clockProvider cannot be null");
        this.idGenerator = Objects.requireNonNull(idGenerator, "eventIdSupplier cannot be null");
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
            final String topic, final String eventType, final IdGenerator<String> eventIdSupplier) {
        return new EventEnvelopeTemplate(topic, eventType, ClockProvider.Asia.SHANGHAI, eventIdSupplier);
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
                topic, eventType, ClockProvider.Asia.SHANGHAI, UuidStringIdGenerator.INSTANCE);
    }

    /**
     * 将载荷封装为新的事件信封。
     *
     * @param <T> 载荷类型
     * @param payload 事件载荷
     * @return 带有新生成标识和当前时间戳的事件信封
     */
    public <T> EventEnvelope<T> wrap(final T payload) {
        return new EventEnvelope<>(this.topic, this.eventType, this.idGenerator.nextId(), clockProvider.instant(),
                payload);
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name + " cannot be null").isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}

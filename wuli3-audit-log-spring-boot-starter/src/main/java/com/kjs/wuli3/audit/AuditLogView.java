package com.kjs.wuli3.audit;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import java.time.Instant;
import java.util.Objects;

/**
 * 独立审计服务持久化后返回的审计日志视图。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogView(long logId, String eventId, Instant occurredAt, Instant storedAt, AuditLogPayload payload) {

    public AuditLogView {
        if (logId < 1) {
            throw new IllegalArgumentException("logId must be greater than zero");
        }
        if (Objects.requireNonNull(eventId, "eventId").isBlank()) {
            throw new IllegalArgumentException("eventId cannot be blank");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(storedAt, "storedAt");
        Objects.requireNonNull(payload, "payload");
    }

    /** 使用已持久化的事件信封构造查询视图。 */
    public static AuditLogView from(
            final long logId, final Instant storedAt, final EventEnvelope<AuditLogPayload> envelope) {
        final EventEnvelope<AuditLogPayload> actualEnvelope = Objects.requireNonNull(envelope, "envelope");
        return new AuditLogView(
                logId, actualEnvelope.eventId(), actualEnvelope.occurredOn(), storedAt, actualEnvelope.payload());
    }
}

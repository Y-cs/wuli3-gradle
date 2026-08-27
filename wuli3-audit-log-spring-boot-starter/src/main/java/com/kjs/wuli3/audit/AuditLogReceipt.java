package com.kjs.wuli3.audit;

import java.time.Instant;
import java.util.Objects;

/**
 * 审计事件被写入 Transport 后返回的生产者侧回执。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogReceipt(String eventId, Instant occurredAt) {

    public AuditLogReceipt {
        if (Objects.requireNonNull(eventId, "eventId").isBlank()) {
            throw new IllegalArgumentException("eventId cannot be blank");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}

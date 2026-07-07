package com.kjs.wuli3.event;

import java.time.Instant;
import java.util.UUID;

public record BasicDomainEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        int version,
        EventMetadata metadata,
        String aggregateId,
        String aggregateType)
        implements DomainEvent {
    public BasicDomainEvent {
        if (version < 1) {
            throw new IllegalArgumentException("version must be greater than 0");
        }
    }

    public static BasicDomainEvent create(String eventType, String aggregateId, String aggregateType) {
        return new BasicDomainEvent(
                UUID.randomUUID(), Instant.now(), eventType, 1, EventMetadata.empty(), aggregateId, aggregateType);
    }
}

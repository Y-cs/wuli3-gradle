package com.kjs.wuli3.event;

import java.time.Instant;
import java.util.UUID;

public record BasicIntegrationEvent(
        UUID eventId, Instant occurredAt, String eventType, int version, EventMetadata metadata, String sourceService)
        implements IntegrationEvent {
    public BasicIntegrationEvent {
        if (version < 1) {
            throw new IllegalArgumentException("version must be greater than 0");
        }
    }

    public static BasicIntegrationEvent create(String eventType, String sourceService) {
        return new BasicIntegrationEvent(
                UUID.randomUUID(), Instant.now(), eventType, 1, EventMetadata.empty(), sourceService);
    }
}

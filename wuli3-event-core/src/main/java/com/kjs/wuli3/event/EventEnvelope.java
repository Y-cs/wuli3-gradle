package com.kjs.wuli3.event;

import java.time.Instant;
import java.util.UUID;

public interface EventEnvelope {
    UUID eventId();

    Instant occurredAt();

    String eventType();

    int version();

    EventMetadata metadata();
}

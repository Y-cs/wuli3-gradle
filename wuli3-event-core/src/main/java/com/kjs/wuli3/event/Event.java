package com.kjs.wuli3.event;

import java.time.Instant;
import java.util.UUID;

/** Stable identity shared by local domain events and cross-service integration events. */
public interface Event {
    /**
     * Globally unique identity used for correlation and deduplication.
     *
     * @return event identity
     */
    UUID eventId();

    /**
     * Time when the business fact occurred, rather than when it was published or consumed.
     *
     * @return occurrence time
     */
    Instant occurredAt();
}

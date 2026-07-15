package com.kjs.wuli3.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventCoreTest {
    @Test
    void metadataDefensivelyCopiesHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("correlation-id", "request-1");

        final EventMetadata metadata = new EventMetadata(headers);
        headers.put("causation-id", "command-1");

        assertThat(metadata.headers()).containsExactly(Map.entry("correlation-id", "request-1"));
        assertThatThrownBy(() -> metadata.headers().put("tenant-id", "tenant-1"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void concreteEventMayExplicitlyHaveDomainAndIntegrationRoles() {
        final UUID eventId = UUID.randomUUID();
        final Instant occurredAt = Instant.now();
        final OrderCreatedEvent event = new OrderCreatedEvent(
                eventId, occurredAt, "order.created", 1, "order-service", EventMetadata.empty(), "order-1");

        assertThat(event).isInstanceOf(DomainEvent.class).isInstanceOf(IntegrationEvent.class);
        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.occurredAt()).isEqualTo(occurredAt);
        assertThat(event.eventName()).isEqualTo("order.created");
        assertThat(event.schemaVersion()).isEqualTo(1);
        assertThat(event.producerService()).isEqualTo("order-service");
        assertThat(event.metadata().headers()).isEmpty();
        assertThat(event.orderId()).isEqualTo("order-1");
    }

    private record OrderCreatedEvent(
            UUID eventId,
            Instant occurredAt,
            String eventName,
            int schemaVersion,
            String producerService,
            EventMetadata metadata,
            String orderId)
            implements DomainEvent, IntegrationEvent {}
}

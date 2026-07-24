package com.kjs.wuli3.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EventEnvelopeTest {

    @Test
    void takesAShallowHeaderSnapshot() {
        final Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("tenant", "acme");

        final EventEnvelope<String> envelope =
                new EventEnvelope<>(headers, "orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
        headers.put("tenant", "changed");
        headers.put("other", "value");

        assertThat(envelope.headers()).containsExactly(Map.entry("tenant", "acme"));
    }

    @Test
    void rejectsBlankProtocolIdentifiers() {
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> new EventEnvelope<>(Map.of(), " ", "order.paid.v1", "event-1", Instant.EPOCH, "payload"))
                .withMessage("topic cannot be blank");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EventEnvelope<>(Map.of(), "orders", " ", "event-1", Instant.EPOCH, "payload"))
                .withMessage("eventType cannot be blank");
    }

    @Test
    @SuppressWarnings("NullAway")
    void rejectsNullPayload() {
        assertThatNullPointerException()
                .isThrownBy(
                        () -> new EventEnvelope<>(Map.of(), "orders", "order.paid.v1", "event-1", Instant.EPOCH, null))
                .withMessage("payload cannot be null");
    }

    @Test
    void templateValidatesTopicBeforePublishing() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> EventEnvelopeTemplate.of(" ", "order.paid.v1"))
                .withMessage("topic cannot be blank");
    }
}

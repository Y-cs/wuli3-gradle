package com.kjs.wuli3.event;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class EventEnvelopeTest {

    @Test
    void rejectsBlankProtocolIdentifiers() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EventEnvelope<>(" ", "order.paid.v1", "event-1", Instant.EPOCH, "payload"))
                .withMessage("topic cannot be blank");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EventEnvelope<>("orders", " ", "event-1", Instant.EPOCH, "payload"))
                .withMessage("eventType cannot be blank");
    }

    @Test
    @SuppressWarnings("NullAway")
    void rejectsNullPayload() {
        assertThatNullPointerException()
                .isThrownBy(() -> new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, null))
                .withMessage("payload cannot be null");
    }

    @Test
    void templateValidatesTopicBeforePublishing() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> EventEnvelopeTemplate.of(" ", "order.paid.v1"))
                .withMessage("topic cannot be blank");
    }
}

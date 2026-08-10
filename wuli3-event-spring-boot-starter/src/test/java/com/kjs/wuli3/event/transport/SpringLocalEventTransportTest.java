package com.kjs.wuli3.event.transport;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.options.SpringLocalPublishOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpringLocalEventTransportTest {

    @Test
    void publishesEveryEnvelopeThroughSpring() {
        final List<Object> published = new ArrayList<>();
        final SpringLocalEventTransport transport = new SpringLocalEventTransport(published::add);
        final EventEnvelope<String> first = SpringLocalEventTransportTest.envelope("event-1");
        final EventEnvelope<String> second = SpringLocalEventTransportTest.envelope("event-2");

        transport.send(new SpringLocalPublishOptions(false, false), first, second);

        assertThat(published).containsExactly(first, second);
        assertThat(transport.supportedOptionsType()).isEqualTo(SpringLocalPublishOptions.class);
    }

    private static EventEnvelope<String> envelope(final String eventId) {
        return new EventEnvelope<>("orders", "order.paid.v1", eventId, Instant.EPOCH, "payload");
    }
}

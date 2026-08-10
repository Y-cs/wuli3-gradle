package com.kjs.wuli3.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.error.UnsupportedCapabilityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoutingEventPublisherTest {

    @Test
    void routesByTheConcreteOptionsType() {
        final RecordingTransport transport = new RecordingTransport();
        final RoutingEventPublisher publisher = new RoutingEventPublisher();
        publisher.register(transport);

        publisher.publish(new TestOptions(), RoutingEventPublisherTest.envelope("event-1"));

        assertThat(transport.sent).extracting(EventEnvelope::eventId).containsExactly("event-1");
    }

    @Test
    void rejectsAnUnregisteredOptionsType() {
        final RoutingEventPublisher publisher = new RoutingEventPublisher();

        assertThatThrownBy(() -> publisher.publish(new UnknownOptions(), RoutingEventPublisherTest.envelope("event-1")))
                .isInstanceOf(UnsupportedCapabilityException.class)
                .hasMessageContaining(UnknownOptions.class.getName());
    }

    @Test
    void rejectsASecondTransportForTheSameOptionsType() {
        final RoutingEventPublisher publisher = new RoutingEventPublisher();
        publisher.register(new RecordingTransport());

        assertThatThrownBy(() -> publisher.register(new RecordingTransport()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(TestOptions.class.getName());
    }

    private static EventEnvelope<String> envelope(final String eventId) {
        return new EventEnvelope<>("orders", "order.paid.v1", eventId, Instant.EPOCH, "payload");
    }

    private record TestOptions() implements PublishOptions {}

    private record UnknownOptions() implements PublishOptions {}

    private static final class RecordingTransport implements EventTransport<TestOptions> {

        private final List<EventEnvelope<?>> sent = new ArrayList<>();

        @Override
        public Class<TestOptions> supportedOptionsType() {
            return TestOptions.class;
        }

        @Override
        public void send(final TestOptions options, final EventEnvelope<?>... envelopes) {
            this.sent.addAll(List.of(envelopes));
        }
    }
}

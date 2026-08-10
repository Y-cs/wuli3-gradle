package com.kjs.wuli3.event.transport;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.options.SpringLocalPublishOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AsyncEventTransportTest {

    @Test
    void snapshotsAndDefersAnAsyncRequest() {
        final RecordingTransport delegate = new RecordingTransport();
        final List<Runnable> tasks = new ArrayList<>();
        final AsyncEventTransport<SpringLocalPublishOptions> transport =
                new AsyncEventTransport<>(delegate, tasks::add);
        final EventEnvelope<?>[] envelopes = {AsyncEventTransportTest.envelope("event-1")};

        transport.send(new SpringLocalPublishOptions(true, false), envelopes);
        envelopes[0] = AsyncEventTransportTest.envelope("event-2");

        assertThat(delegate.sent).isEmpty();
        assertThat(tasks).hasSize(1);
        tasks.getFirst().run();
        assertThat(delegate.sent).extracting(EventEnvelope::eventId).containsExactly("event-1");
    }

    private static EventEnvelope<String> envelope(final String eventId) {
        return new EventEnvelope<>("orders", "order.paid.v1", eventId, Instant.EPOCH, "payload");
    }

    private static final class RecordingTransport implements EventTransport<SpringLocalPublishOptions> {

        private final List<EventEnvelope<?>> sent = new ArrayList<>();

        @Override
        public Class<SpringLocalPublishOptions> supportedOptionsType() {
            return SpringLocalPublishOptions.class;
        }

        @Override
        public void send(final SpringLocalPublishOptions options, final EventEnvelope<?>... envelopes) {
            this.sent.addAll(List.of(envelopes));
        }
    }
}

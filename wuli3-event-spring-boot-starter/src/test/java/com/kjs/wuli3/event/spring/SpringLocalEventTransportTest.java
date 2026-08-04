package com.kjs.wuli3.event.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventTransport.UnsupportedCapabilityException;
import com.kjs.wuli3.event.PublishOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpringLocalEventTransportTest {

    @Test
    void publishesImmediatelyThroughSpring() {
        final List<Object> published = new ArrayList<>();
        final SpringLocalEventTransport transport = new SpringLocalEventTransport(published::add);
        final EventEnvelope<String> envelope = SpringLocalEventTransportTest.envelope("event-1");

        transport.send(envelope, PublishOptions.defaults());

        assertThat(published).containsExactly(envelope);
    }

    @Test
    void rejectsRemoteOrExecutionOptions() {
        final SpringLocalEventTransport transport = new SpringLocalEventTransport(event -> {});
        final EventEnvelope<String> envelope = SpringLocalEventTransportTest.envelope("event-1");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> transport.send(envelope, new PublishOptions(PublishOptions.Channel.REMOTE)))
                .withMessage("Spring local transport requires the LOCAL channel");
        assertThatThrownBy(
                        () -> transport.send(envelope, PublishOptions.defaults().async()))
                .isInstanceOf(UnsupportedCapabilityException.class);
    }

    private static EventEnvelope<String> envelope(final String eventId) {
        return new EventEnvelope<>("orders", "order.paid.v1", eventId, Instant.EPOCH, "payload");
    }
}

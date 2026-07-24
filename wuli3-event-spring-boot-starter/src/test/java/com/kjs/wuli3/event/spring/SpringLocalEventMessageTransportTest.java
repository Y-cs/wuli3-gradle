package com.kjs.wuli3.event.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventMessageTransport.UnsupportedCapabilityException;
import com.kjs.wuli3.event.PublishOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpringLocalEventMessageTransportTest {

    @Test
    void publishesImmediatelyThroughSpring() {
        final List<Object> published = new ArrayList<>();
        final SpringLocalEventMessageTransport transport = new SpringLocalEventMessageTransport(published::add);
        final EventEnvelope<String> envelope = SpringLocalEventMessageTransportTest.envelope("event-1");

        transport.send(envelope, PublishOptions.defaults());

        assertThat(published).containsExactly(envelope);
    }

    @Test
    void rejectsRemoteOrExecutionOptions() {
        final SpringLocalEventMessageTransport transport = new SpringLocalEventMessageTransport(event -> {});
        final EventEnvelope<String> envelope = SpringLocalEventMessageTransportTest.envelope("event-1");

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

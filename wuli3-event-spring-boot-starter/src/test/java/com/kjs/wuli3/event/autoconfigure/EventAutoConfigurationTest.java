package com.kjs.wuli3.event.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventMessageTransport;
import java.time.Instant;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class EventAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(EventAutoConfiguration.class));

    @Test
    void failsExplicitlyWhenRemotePublicationHasNoTransport() {
        this.contextRunner.run(context -> {
            final EventPublisher publisher = context.getBean(EventPublisher.class);

            assertThatIllegalStateException()
                    .isThrownBy(() -> publisher.publish(
                            EventAutoConfigurationTest.envelope(), new PublishOptions(PublishOptions.Channel.REMOTE)))
                    .withMessage("No RemoteEventMessageTransport is configured");
        });
    }

    @Test
    void usesTheApplicationRemoteTransportWhenProvided() {
        this.contextRunner
                .withBean(RemoteEventMessageTransport.class, RecordingRemoteTransport::new)
                .run(context -> {
                    final EventPublisher publisher = context.getBean(EventPublisher.class);
                    final RecordingRemoteTransport transport = context.getBean(RecordingRemoteTransport.class);

                    publisher.publish(
                            EventAutoConfigurationTest.envelope(), new PublishOptions(PublishOptions.Channel.REMOTE));

                    assertThat(transport.sent).containsExactly(EventAutoConfigurationTest.envelope());
                });
    }

    private static EventEnvelope<String> envelope() {
        return new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
    }

    private static final class RecordingRemoteTransport implements RemoteEventMessageTransport {

        private final java.util.List<EventEnvelope<?>> sent = new java.util.ArrayList<>();

        @Override
        public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
            this.sent.add(envelope);
        }

        @Override
        public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
            this.sent.addAll(envelopes);
        }
    }
}

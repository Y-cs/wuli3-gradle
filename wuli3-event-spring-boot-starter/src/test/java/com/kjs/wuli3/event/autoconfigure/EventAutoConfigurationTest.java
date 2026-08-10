package com.kjs.wuli3.event.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.error.UnsupportedCapabilityException;
import com.kjs.wuli3.event.options.SpringLocalPublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.PayloadApplicationEvent;

class EventAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(EventAutoConfiguration.class));

    @Test
    void registersAndPublishesLocalEventsWithoutARemoteTransport() {
        this.contextRunner.run(context -> {
            final List<Object> published = new ArrayList<>();
            context.addApplicationListener(event -> {
                if (event instanceof PayloadApplicationEvent<?> payloadEvent
                        && payloadEvent.getPayload() instanceof EventEnvelope<?>) {
                    published.add(payloadEvent.getPayload());
                }
            });
            final EventPublisher publisher = context.getBean(EventPublisher.class);
            final EventEnvelope<String> envelope = EventAutoConfigurationTest.envelope();

            publisher.publish(new SpringLocalPublishOptions(false, false), envelope);

            assertThat(published).containsExactly(envelope);
            assertThatThrownBy(() -> publisher.publish(new SpringLocalPublishOptions(true, false), envelope))
                    .isInstanceOf(UnsupportedCapabilityException.class)
                    .hasMessageContaining("applicationTaskExecutor");
            assertThatThrownBy(() -> publisher.publish(new UnknownOptions(), envelope))
                    .isInstanceOf(UnsupportedCapabilityException.class);
        });
    }

    @Test
    void registersAnApplicationRemoteTransport() {
        this.contextRunner
                .withBean(RecordingRemoteTransport.class, RecordingRemoteTransport::new)
                .run(context -> {
                    final EventPublisher publisher = context.getBean(EventPublisher.class);
                    final RecordingRemoteTransport transport = context.getBean(RecordingRemoteTransport.class);

                    publisher.publish(new RemoteOptions(), EventAutoConfigurationTest.envelope());

                    assertThat(transport.sent).containsExactly(EventAutoConfigurationTest.envelope());
                });
    }

    private static EventEnvelope<String> envelope() {
        return new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
    }

    private record RemoteOptions() implements PublishOptions {}

    private record UnknownOptions() implements PublishOptions {}

    private static final class RecordingRemoteTransport implements RemoteEventTransport<RemoteOptions> {

        private final List<EventEnvelope<?>> sent = new ArrayList<>();

        @Override
        public Class<RemoteOptions> supportedOptionsType() {
            return RemoteOptions.class;
        }

        @Override
        public void send(final RemoteOptions options, final EventEnvelope<?>... envelopes) {
            this.sent.addAll(List.of(envelopes));
        }
    }
}

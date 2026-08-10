package com.kjs.wuli3.consumer;

import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.envelope.EventEnvelopeTemplate;
import com.kjs.wuli3.json.provider.JacksonProvider;
import java.time.Instant;

public final class MavenConsumer {
    private MavenConsumer() {}

    public static String createIdentifier() {
        JacksonProvider.newJsonMapper();
        final EventEnvelope<ConsumerEvent> envelope =
                EventEnvelopeTemplate.of("consumer-events", "consumer.event.v1").wrap(new ConsumerEvent(Instant.now()));
        envelope.eventId();
        EventPublisher.class.getName();
        assertJavaClientPreviewDependencyIsNotPresent();
        return UuidStringIdGenerator.INSTANCE.nextId();
    }

    private static void assertJavaClientPreviewDependencyIsNotPresent() {
        try {
            Class.forName("org.apache.rocketmq.client.apis.ClientServiceProvider");
        } catch (ClassNotFoundException expected) {
            return;
        }
        throw new IllegalStateException("rocketmq-client-java must not be a published runtime dependency");
    }

    private record ConsumerEvent(Instant occurredOn) {}
}

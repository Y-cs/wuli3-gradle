package com.kjs.wuli3.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventEnvelopeTemplate;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.json.provider.JacksonProvider;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BomConsumerTest {
    @Test
    void resolvesAndUsesVersionlessComponents() {
        assertNotNull(UuidStringIdGenerator.INSTANCE.nextId());
        assertNotNull(JacksonProvider.newJsonMapper());
        final EventEnvelope<ConsumerEvent> envelope =
                EventEnvelopeTemplate.of("consumer-events", "consumer.event.v1").wrap(new ConsumerEvent(Instant.now()));
        assertNotNull(envelope.eventId());
        assertNotNull(EventPublisher.class);
        org.junit.jupiter.api.Assertions.assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("org.apache.rocketmq.client.apis.ClientServiceProvider"));
    }

    private record ConsumerEvent(Instant occurredOn) {}
}

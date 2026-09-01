package com.kjs.wuli3.rabbit.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class RabbitMessageEncoderTest {

    @Test
    void storesPropagationHeadersOutsideTheSerializedEnvelope() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
        final ContextPropagator contextPropagator = new ContextPropagator(ContextPropagator.standardContextEncoder());
        final EventEnvelope<String> envelope =
                new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");

        final Message message = new RabbitMessageEncoder(contextStore, contextPropagator).encode(envelope);
        final String body = new String(message.getBody(), StandardCharsets.UTF_8);
        final MessageProperties properties = message.getMessageProperties();

        assertThat(properties.getHeaders())
                .containsEntry("X-Request-Id", "request-42")
                .containsEntry("X-Origin-Ip", "10.0.0.8");
        assertThat(properties.getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
        assertThat(properties.getMessageId()).isEqualTo("event-1");
        assertThat(properties.getType()).isEqualTo("order.paid.v1");
        assertThat(body).contains("\"topic\":\"orders\"");
        assertThat(body).doesNotContain("headers", "X-Request-Id", "request-42");
    }

    @Test
    void encodesWithoutPropagationHeadersWhenNoReaderIsAvailable() {
        final EventEnvelope<String> envelope =
                new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");

        final Message message = new RabbitMessageEncoder(null, new ContextPropagator(List.of())).encode(envelope);

        assertThat(message.getMessageProperties().getHeaders()).isEmpty();
    }
}

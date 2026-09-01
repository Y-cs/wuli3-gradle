package com.kjs.wuli3.rocket.internal.wrapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.rocket.internal.RocketPublishOptions;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RocketMessageWrapperEncoderTest {

    @Test
    void storesPropagationHeadersOutsideTheSerializedEnvelope() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
        final ContextPropagator contextPropagator = new ContextPropagator(ContextPropagator.standardContextEncoder());
        final EventEnvelope<String> envelope =
                new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");

        final RocketMessageWrapper wrapper = new RocketMessageWrapperEncoder(contextStore, contextPropagator)
                .encode(envelope, new RocketPublishOptions());
        final String body = new String(wrapper.body(), StandardCharsets.UTF_8);

        assertThat(wrapper.headers())
                .containsEntry("X-Request-Id", "request-42")
                .containsEntry("X-Origin-Ip", "10.0.0.8");
        assertThat(body).contains("\"topic\":\"orders\"");
        assertThat(body).doesNotContain("headers", "X-Request-Id", "request-42");
    }
}

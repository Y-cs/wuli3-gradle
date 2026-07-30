package com.kjs.wuli3.rocketmq.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventMessageTransport.UnsupportedCapabilityException;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.propagation.codec.DefaultPropagationContextCodecs;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.propagation.transmission.ContextTransmitter;
import com.kjs.wuli3.rocketmq.autoconfigure.RocketMqContextMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RocketMqEventMessageEncoderTest {

    @Test
    void rebuildsReservedHeadersFromTheContextStore() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
        contextStore.put(new AuthContext(7L, "alice"));
        final EventEnvelope<String> envelope = new EventEnvelope<>(
                Map.of("X-USER-ID", "forged", "X-Request-Id", "forged-request", "business", "value"),
                "orders",
                "order.paid.v1",
                "event-1",
                Instant.EPOCH,
                "payload");

        final RocketMqWireMessage wireMessage = new RocketMqEventMessageEncoder(
                        RocketMqEventMessageEncoderTest.transmitter(contextStore, RocketMqContextMode.INVOCATION_ONLY))
                .encode(envelope, RocketMqEventMessageEncoderTest.remote());
        final String body = new String(wireMessage.body(), StandardCharsets.UTF_8);

        assertThat(body).contains("request-42", "10.0.0.8", "business", "value");
        assertThat(body).doesNotContain("forged", "alice", "X-User-Id");
        assertThat(wireMessage.key()).isEqualTo("event-1");
    }

    @Test
    void trustedModeAddsAuthenticationHeadersFromTheContextStore() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
        contextStore.put(new AuthContext(7L, "alice"));

        final RocketMqWireMessage wireMessage = new RocketMqEventMessageEncoder(
                        RocketMqEventMessageEncoderTest.transmitter(contextStore, RocketMqContextMode.TRUSTED_INTERNAL))
                .encode(RocketMqEventMessageEncoderTest.envelope("orders"), RocketMqEventMessageEncoderTest.remote());

        assertThat(new String(wireMessage.body(), StandardCharsets.UTF_8)).contains("X-User-Id", "7", "alice");
    }

    @Test
    void rejectsInvalidRemoteCapabilitiesBeforeCallingAnSdk() {
        final RocketMqEventMessageEncoder encoder = new RocketMqEventMessageEncoder(null);

        assertThatThrownBy(() -> encoder.encode(
                        RocketMqEventMessageEncoderTest.envelope("orders"),
                        RocketMqEventMessageEncoderTest.remote().async().setDelayTime(Duration.ofSeconds(1))))
                .isInstanceOf(UnsupportedCapabilityException.class);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> encoder.encode(
                        RocketMqEventMessageEncoderTest.envelope("orders:tag"),
                        RocketMqEventMessageEncoderTest.remote()))
                .withMessageContaining("event-1");
    }

    @Test
    void rejectsLocalChannel() {
        final RocketMqEventMessageEncoder encoder = new RocketMqEventMessageEncoder(null);

        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        encoder.encode(RocketMqEventMessageEncoderTest.envelope("orders"), PublishOptions.defaults()))
                .withMessage("RocketMQ transport requires the REMOTE channel");
    }

    private static EventEnvelope<String> envelope(final String topic) {
        return new EventEnvelope<>(topic, "order.paid.v1", "event-1", Instant.EPOCH, "payload");
    }

    private static PublishOptions remote() {
        return new PublishOptions(PublishOptions.Channel.REMOTE);
    }

    private static ContextTransmitter transmitter(
            final ContextStore contextStore, final RocketMqContextMode contextMode) {
        return new ContextTransmitter(
                contextStore,
                contextStore,
                contextMode == RocketMqContextMode.TRUSTED_INTERNAL
                        ? DefaultPropagationContextCodecs.trustedInternal()
                        : DefaultPropagationContextCodecs.invocationOnly());
    }
}

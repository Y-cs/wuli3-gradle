package com.kjs.wuli3.propagation.transmission;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.carrier.MapContextCarrier;
import com.kjs.wuli3.propagation.codec.AuthContextCodec;
import com.kjs.wuli3.propagation.codec.DefaultPropagationContextCodecs;
import com.kjs.wuli3.propagation.codec.InvocationContextCodec;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContextTransmitterTest {

    private final ContextStore holder = new ContextStore();

    @Test
    void invocationOnlyWritesInvocationContext() {
        this.holder.put(new InvocationContext("10.0.0.1", "rid-1"));
        this.holder.put(new AuthContext(42L, "alice"));

        final MapContextCarrier carrier = new MapContextCarrier();
        final ContextTransmitter transmitter =
                new ContextTransmitter(this.holder, this.holder, DefaultPropagationContextCodecs.invocationOnly());

        transmitter.writeTo(carrier);

        assertThat(carrier.asMap())
                .containsEntry(InvocationContextCodec.REQUEST_ID, "rid-1")
                .containsEntry(InvocationContextCodec.ORIGIN_IP, "10.0.0.1")
                .doesNotContainKey(AuthContextCodec.USER_ID);
    }

    @Test
    void trustedInternalWritesAuthContext() {
        this.holder.put(new InvocationContext("10.0.0.1", "rid-1"));
        this.holder.put(new AuthContext(42L, "alice"));

        final MapContextCarrier carrier = new MapContextCarrier();
        final ContextTransmitter transmitter =
                new ContextTransmitter(this.holder, this.holder, DefaultPropagationContextCodecs.trustedInternal());

        transmitter.writeTo(carrier);

        assertThat(carrier.asMap())
                .containsEntry(AuthContextCodec.USER_ID, "42")
                .containsEntry(AuthContextCodec.USERNAME, "alice");
    }

    @Test
    void readsInvocationContextFromCarrier() {
        final MapContextCarrier carrier = new MapContextCarrier(Map.of(
                InvocationContextCodec.REQUEST_ID, "rid-2",
                InvocationContextCodec.ORIGIN_IP, "10.0.0.2"));
        final ContextTransmitter transmitter =
                new ContextTransmitter(this.holder, this.holder, DefaultPropagationContextCodecs.invocationOnly());

        transmitter.readFrom(carrier);

        assertThat(this.holder.get(InvocationContext.class))
                .get()
                .extracting(InvocationContext::getRequestId, InvocationContext::getOriginIp)
                .containsExactly("rid-2", "10.0.0.2");
    }

    @Test
    void readsAuthContextOnlyWhenTrustedInternalCodecsAreUsed() {
        final MapContextCarrier carrier = new MapContextCarrier(Map.of(
                InvocationContextCodec.REQUEST_ID, "rid-3",
                AuthContextCodec.USER_ID, "42",
                AuthContextCodec.USERNAME, "alice"));
        final ContextTransmitter transmitter =
                new ContextTransmitter(this.holder, this.holder, DefaultPropagationContextCodecs.trustedInternal());

        transmitter.readFrom(carrier);

        assertThat(this.holder.get(AuthContext.class))
                .get()
                .extracting(AuthContext::getUserId, AuthContext::getUsername)
                .containsExactly(42L, "alice");
    }

    @Test
    void invalidAuthContextIsIgnored() {
        final MapContextCarrier carrier = new MapContextCarrier(Map.of(
                AuthContextCodec.USER_ID, "invalid",
                AuthContextCodec.USERNAME, "alice"));
        final ContextTransmitter transmitter =
                new ContextTransmitter(this.holder, this.holder, DefaultPropagationContextCodecs.trustedInternal());

        transmitter.readFrom(carrier);

        assertThat(this.holder.get(AuthContext.class)).isEmpty();
    }
}

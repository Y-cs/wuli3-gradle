package com.kjs.wuli3.propagation.transmission;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.carrier.MapContextCarrier;
import com.kjs.wuli3.propagation.codec.DefaultPropagationContextCodecs;
import com.kjs.wuli3.propagation.codec.PropagationFieldNames;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextStore;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ContextTransmitterTest {

    private final ContextStore holder = new ContextStore();

    @Test
    void invocationOnlyWritesInvocationContext() {
        this.holder.put(new InvocationContext("10.0.0.1", "rid-1"));
        this.holder.put(new AuthContext(42L, "alice"));

        final MapContextCarrier carrier = new MapContextCarrier();
        final ContextTransmitter transmitter = new ContextTransmitter(this.holder, this.holder,
                DefaultPropagationContextCodecs.invocationOnly());

        transmitter.writeTo(carrier);

        assertThat(carrier.asMap()).containsEntry(PropagationFieldNames.REQUEST_ID, "rid-1")
                .containsEntry(PropagationFieldNames.ORIGIN_IP, "10.0.0.1")
                .doesNotContainKey(PropagationFieldNames.USER_ID);
    }

    @Test
    void trustedInternalWritesAuthContext() {
        this.holder.put(new InvocationContext("10.0.0.1", "rid-1"));
        this.holder.put(new AuthContext(42L, "alice"));

        final MapContextCarrier carrier = new MapContextCarrier();
        final ContextTransmitter transmitter = new ContextTransmitter(this.holder, this.holder,
                DefaultPropagationContextCodecs.trustedInternal());

        transmitter.writeTo(carrier);

        assertThat(carrier.asMap()).containsEntry(PropagationFieldNames.USER_ID, "42")
                .containsEntry(PropagationFieldNames.USERNAME, "alice");
    }

    @Test
    void readsInvocationContextFromCarrier() {
        final MapContextCarrier carrier = new MapContextCarrier(Map.of(
                PropagationFieldNames.REQUEST_ID, "rid-2",
                PropagationFieldNames.ORIGIN_IP, "10.0.0.2"
        ));
        final ContextTransmitter transmitter = new ContextTransmitter(this.holder, this.holder,
                DefaultPropagationContextCodecs.invocationOnly());

        transmitter.readFrom(carrier);

        assertThat(this.holder.get(InvocationContext.class))
                .get()
                .extracting(InvocationContext::getRequestId, InvocationContext::getOriginIp)
                .containsExactly("rid-2", "10.0.0.2");
    }

    @Test
    void readsAuthContextOnlyWhenTrustedInternalCodecsAreUsed() {
        final MapContextCarrier carrier = new MapContextCarrier(Map.of(
                PropagationFieldNames.REQUEST_ID, "rid-3",
                PropagationFieldNames.USER_ID, "42",
                PropagationFieldNames.USERNAME, "alice"
        ));
        final ContextTransmitter transmitter = new ContextTransmitter(this.holder, this.holder,
                DefaultPropagationContextCodecs.trustedInternal());

        transmitter.readFrom(carrier);

        assertThat(this.holder.get(AuthContext.class))
                .get()
                .extracting(AuthContext::getUserId, AuthContext::getUsername)
                .containsExactly(42L, "alice");
    }

    @Test
    void invalidAuthContextIsIgnored() {
        final MapContextCarrier carrier = new MapContextCarrier(Map.of(
                PropagationFieldNames.USER_ID, "invalid",
                PropagationFieldNames.USERNAME, "alice"
        ));
        final ContextTransmitter transmitter = new ContextTransmitter(this.holder, this.holder,
                DefaultPropagationContextCodecs.trustedInternal());

        transmitter.readFrom(carrier);

        assertThat(this.holder.get(AuthContext.class)).isEmpty();
    }
}

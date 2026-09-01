package com.kjs.wuli3.rabbit.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.codec.AuthContextCodec;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.codec.InvocationContextCodec;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RabbitContextSupportTest {

    @Test
    void decodesHeadersAndRestoresThePreviousContextAfterScopeClose() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("127.0.0.1", "previous"));
        final RabbitContextSupport support =
                new RabbitContextSupport(contextStore, new ContextPropagator(ContextPropagator.standardContextEncoder()));
        final RabbitContextSupport.RabbitContextProxy propagator = support.restoreFrom(Map.of(
                InvocationContextCodec.REQUEST_ID, "request-42",
                InvocationContextCodec.ORIGIN_IP, "10.0.0.8",
                AuthContextCodec.PRINCIPAL_TYPE, "CUSTOMER",
                AuthContextCodec.PRINCIPAL_ID, "7",
                AuthContextCodec.PRINCIPAL_NAME, "alice"));

        assertThat(propagator.capture().get(AuthContext.class))
                .contains(new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        final ContextScope scope = propagator.restore(propagator.capture());
        try {
            assertThat(contextStore.get(InvocationContext.class))
                    .contains(new InvocationContext("10.0.0.8", "request-42"));
            assertThat(contextStore.get(AuthContext.class))
                    .contains(new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        } finally {
            scope.close();
        }

        assertThat(contextStore.get(InvocationContext.class)).contains(new InvocationContext("127.0.0.1", "previous"));
        assertThat(contextStore.get(AuthContext.class)).isEmpty();
    }
}

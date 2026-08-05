package com.kjs.wuli3.rocket.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.encoding.AuthContextEncoder;
import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.encoding.InvocationContextEncoder;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RocketContextSupportTest {

    @Test
    void decodesHeadersAndRestoresThePreviousContextAfterScopeClose() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("127.0.0.1", "previous"));
        final RocketContextSupport support =
                new RocketContextSupport(contextStore, new ContextEncoder(ContextEncoder.standardContextEncoder()));
        final RocketContextSupport.RocketContextPropagator propagator = support.restoreFrom(Map.of(
                InvocationContextEncoder.REQUEST_ID, "request-42",
                InvocationContextEncoder.ORIGIN_IP, "10.0.0.8",
                AuthContextEncoder.USER_ID, 7L,
                AuthContextEncoder.USERNAME, "alice"));

        assertThat(propagator.capture().get(AuthContext.class)).contains(new AuthContext(7L, "alice"));
        final ContextScope scope = propagator.restore(propagator.capture());
        try {
            assertThat(contextStore.get(InvocationContext.class))
                    .contains(new InvocationContext("10.0.0.8", "request-42"));
            assertThat(contextStore.get(AuthContext.class)).contains(new AuthContext(7L, "alice"));
        } finally {
            scope.close();
        }

        assertThat(contextStore.get(InvocationContext.class)).contains(new InvocationContext("127.0.0.1", "previous"));
        assertThat(contextStore.get(AuthContext.class)).isEmpty();
    }
}

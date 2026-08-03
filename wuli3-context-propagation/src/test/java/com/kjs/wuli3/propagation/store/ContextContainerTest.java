package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextContainerTest {

    @Test
    void captureContainsOnlyPropagationContexts() {
        final ContextContainer container = new ContextContainer();
        final AuthContext authContext = new AuthContext(7L, "alice");
        container.put(new LocalOnlyContext("request-cache"));
        container.put(authContext);

        final ContextSnapshot snapshot = container.capture();

        assertThat(snapshot.get(AuthContext.class)).contains(authContext);
        assertThat(snapshot.values()).containsExactly(authContext);
        assertThat(snapshot.isEmpty()).isFalse();
    }

    @Test
    void emptySnapshotRepresentsNoPropagationContexts() {
        final ContextSnapshot snapshot = ContextSnapshot.empty();

        assertThat(snapshot.isEmpty()).isTrue();
        assertThat(snapshot.values()).isEmpty();
    }

    @Test
    void emptyCreatesAnIndependentMutableContainer() {
        final ContextContainer first = new ContextContainer();
        final ContextContainer second = new ContextContainer();

        first.put(new LocalOnlyContext("request-cache"));

        assertThat(first.get(LocalOnlyContext.class)).isPresent();
        assertThat(second.get(LocalOnlyContext.class)).isEmpty();
    }

    private record LocalOnlyContext(String value) implements Context {

        @Override
        public Class<? extends Context> type() {
            return LocalOnlyContext.class;
        }
    }
}

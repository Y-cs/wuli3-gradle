package com.kjs.wuli3.propagation.store;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.DefaultContextPropagator;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

class ContextStoreTest {

    @Test
    void captureExcludesLocalContextAndRestorePreservesPreviousLocalState() {
        final ContextStore store = new ContextStore();
        store.put(new LocalContext("source"));
        store.put(new AuthContext(7L, "alice"));
        final ContextSnapshot snapshot = store.capture();

        store.clear();
        store.put(new LocalContext("target"));
        final ContextScope scope = store.restore(snapshot);
        try {
            assertThat(store.get(AuthContext.class)).contains(new AuthContext(7L, "alice"));
            assertThat(store.get(LocalContext.class)).isEmpty();
        } finally {
            scope.close();
        }

        assertThat(store.get(AuthContext.class)).isEmpty();
        assertThat(store.get(LocalContext.class)).contains(new LocalContext("target"));
    }

    @Test
    void nestedRestoreScopesReturnToEachPreviousContext() {
        final ContextStore store = new ContextStore();
        store.put(new AuthContext(1L, "first"));

        final ContextScope outerScope = store.restore(ContextSnapshot.of(new AuthContext(2L, "second")));
        try {
            assertThat(store.get(AuthContext.class)).contains(new AuthContext(2L, "second"));
            final ContextScope innerScope = store.restore(ContextSnapshot.of(new AuthContext(3L, "third")));
            try {
                assertThat(store.get(AuthContext.class)).contains(new AuthContext(3L, "third"));
            } finally {
                innerScope.close();
            }
            assertThat(store.get(AuthContext.class)).contains(new AuthContext(2L, "second"));
        } finally {
            outerScope.close();
        }

        assertThat(store.get(AuthContext.class)).contains(new AuthContext(1L, "first"));
    }

    @Test
    void wrappedCallableUsesCapturedContextAndRestoresCallerContext() throws Exception {
        final ContextStore store = new ContextStore();
        final DefaultContextPropagator propagator = new DefaultContextPropagator(store);
        store.put(new AuthContext(1L, "captured"));
        final Callable<Long> wrapped = propagator.wrap(
                () -> store.get(AuthContext.class).map(AuthContext::userId).orElseThrow());
        store.put(new AuthContext(2L, "caller"));

        assertThat(wrapped.call()).isEqualTo(1L);
        assertThat(store.get(AuthContext.class)).contains(new AuthContext(2L, "caller"));
    }

    private record LocalContext(String value) implements Context {

        @Override
        public Class<? extends Context> type() {
            return LocalContext.class;
        }
    }
}

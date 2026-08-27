package com.kjs.wuli3.propagation.store;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.DefaultContextPropagator;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

class ContextStoreTest {

    @Test
    void captureExcludesLocalContextAndRestorePreservesPreviousLocalState() {
        final ContextStore store = new ContextStore();
        store.put(new LocalContext("source"));
        store.put(new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        final ContextSnapshot snapshot = store.capture();

        store.clear();
        store.put(new LocalContext("target"));
        final ContextScope scope = store.restore(snapshot);
        try {
            assertThat(store.get(AuthContext.class)).contains(new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
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
        store.put(new AuthContext(PrincipalType.CUSTOMER, "1", "first"));

        final ContextScope outerScope =
                store.restore(ContextSnapshot.of(new AuthContext(PrincipalType.ADMIN, "2", "second")));
        try {
            assertThat(store.get(AuthContext.class)).contains(new AuthContext(PrincipalType.ADMIN, "2", "second"));
            final ContextScope innerScope =
                    store.restore(ContextSnapshot.of(new AuthContext(PrincipalType.SYSTEM, "3", "third")));
            try {
                assertThat(store.get(AuthContext.class)).contains(new AuthContext(PrincipalType.SYSTEM, "3", "third"));
            } finally {
                innerScope.close();
            }
            assertThat(store.get(AuthContext.class)).contains(new AuthContext(PrincipalType.ADMIN, "2", "second"));
        } finally {
            outerScope.close();
        }

        assertThat(store.get(AuthContext.class)).contains(new AuthContext(PrincipalType.CUSTOMER, "1", "first"));
    }

    @Test
    void wrappedCallableUsesCapturedContextAndRestoresCallerContext() throws Exception {
        final ContextStore store = new ContextStore();
        final DefaultContextPropagator propagator = new DefaultContextPropagator(store);
        store.put(new AuthContext(PrincipalType.CUSTOMER, "1", "captured"));
        final Callable<String> wrapped = propagator.wrap(
                () -> store.get(AuthContext.class).map(AuthContext::principalId).orElseThrow());
        store.put(new AuthContext(PrincipalType.CUSTOMER, "2", "caller"));

        assertThat(wrapped.call()).isEqualTo("1");
        assertThat(store.get(AuthContext.class)).contains(new AuthContext(PrincipalType.CUSTOMER, "2", "caller"));
    }

    private record LocalContext(String value) implements Context {

        @Override
        public Class<? extends Context> type() {
            return LocalContext.class;
        }
    }
}

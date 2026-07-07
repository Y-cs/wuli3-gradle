package com.kjs.wuli3.propagation.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.store.ContextContainer;
import com.kjs.wuli3.propagation.store.ContextStore;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

class DefaultContextPropagatorTest {

    private final ContextStore holder = new ContextStore();
    private final DefaultContextPropagator propagator = new DefaultContextPropagator(holder);

    @Test
    void captureIsIsolatedFromLaterCurrentContextChanges() {
        holder.put(authContext(1L));
        ContextSnapshot snapshot = propagator.capture();

        holder.put(authContext(2L));

        this.withRestored(snapshot, () -> {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(1L);
        });
    }

    @Test
    void restoreRestoresPreviousContextWhenScopeClosed() {
        holder.put(authContext(1L));
        ContextSnapshot snapshot = snapshotWith(authContext(2L));

        this.withRestored(snapshot, () -> {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);
        });

        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(1L);
    }

    @Test
    void restoreSupportsNestedScopes() {
        holder.put(authContext(1L));
        ContextSnapshot second = snapshotWith(authContext(2L));
        ContextSnapshot third = snapshotWith(authContext(3L));

        this.withRestored(second, () -> {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);

            this.withRestored(third, () -> {
                assertThat(holder.get(AuthContext.class))
                        .map(AuthContext::getUserId)
                        .contains(3L);
            });

            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);
        });

        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(1L);
    }

    @Test
    void restoreClearsContextWhenNoPreviousContextExists() {
        ContextSnapshot snapshot = snapshotWith(authContext(2L));

        this.withRestored(snapshot, () -> {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);
        });

        assertThat(holder.get(AuthContext.class)).isEmpty();
    }

    @Test
    void restoredContainerDoesNotMutateSnapshot() {
        ContextSnapshot snapshot = snapshotWith(authContext(1L));

        this.withRestored(snapshot, () -> {
            holder.put(authContext(2L));
        });

        this.withRestored(snapshot, () -> {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(1L);
        });
    }

    @Test
    void snapshotDoesNotExposeMutableContainer() {
        final ContextContainer container = new ContextContainer();
        container.put(authContext(1L));
        final ContextSnapshot snapshot = new ContextSnapshot(container);

        container.put(authContext(2L));
        snapshot.getContextContainer()
                .put(authContext(3L));

        this.withRestored(snapshot, () -> {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(1L);
        });
    }

    @Test
    void wrappedRunnableRestoresCapturedContext() {
        holder.put(authContext(1L));
        final Runnable wrapped = propagator.wrap(() -> {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(1L);
        });

        holder.put(authContext(2L));

        wrapped.run();

        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(2L);
    }

    @Test
    void wrappedCallableRestoresCapturedContext() throws Exception {
        holder.put(authContext(1L));
        final Callable<Long> wrapped = propagator.wrap(() -> holder.get(AuthContext.class)
                .map(AuthContext::getUserId)
                .orElse(-1L));

        holder.put(authContext(2L));

        assertThat(wrapped.call()).isEqualTo(1L);
        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(2L);
    }

    @Test
    void wrappedSupplierRestoresCapturedContext() {
        holder.put(authContext(1L));
        final Supplier<Long> wrapped = propagator.wrapSupplier(() -> holder.get(AuthContext.class)
                .map(AuthContext::getUserId)
                .orElse(-1L));

        holder.put(authContext(2L));

        assertThat(wrapped.get()).isEqualTo(1L);
        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(2L);
    }

    @Test
    void containerUsesContextTypeAsStorageKey() {
        holder.put(new CustomAuthContext(1L, "user-1"));

        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(1L);
    }

    private static ContextSnapshot snapshotWith(AuthContext authContext) {
        ContextStore h = new ContextStore();
        h.put(authContext);
        return new DefaultContextPropagator(h).capture();
    }

    private void withRestored(final ContextSnapshot snapshot, final Runnable task) {
        final ContextScope scope = this.propagator.restore(snapshot);
        try {
            task.run();
        } finally {
            scope.close();
        }
    }

    private static AuthContext authContext(Long userId) {
        return new AuthContext(userId, "user-" + userId);
    }

    private static final class CustomAuthContext extends AuthContext {

        CustomAuthContext(Long userId, String username) {
            super(userId, username);
        }

        @Override
        public Class<? extends Context> type() {
            return AuthContext.class;
        }
    }
}

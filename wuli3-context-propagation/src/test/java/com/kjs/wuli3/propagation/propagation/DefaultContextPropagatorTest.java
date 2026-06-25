package com.kjs.wuli3.propagation.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.holder.ContextHolder;
import com.kjs.wuli3.propagation.holder.ContextHolderOperator;
import org.junit.jupiter.api.Test;

class DefaultContextPropagatorTest {

    private final ContextHolder holder = new ContextHolder();
    private final ContextHolderOperator operator = new ContextHolderOperator(holder);
    private final DefaultContextPropagator propagator = new DefaultContextPropagator(holder);

    @Test
    void captureIsIsolatedFromLaterCurrentContextChanges() {
        operator.put(authContext(1L));
        ContextSnapshot snapshot = propagator.capture();

        operator.put(authContext(2L));

        try (ContextScope ignored = propagator.restore(snapshot)) {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(1L);
        }
    }

    @Test
    void restoreRestoresPreviousContextWhenScopeClosed() {
        operator.put(authContext(1L));
        ContextSnapshot snapshot = snapshotWith(authContext(2L));

        try (ContextScope ignored = propagator.restore(snapshot)) {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);
        }

        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(1L);
    }

    @Test
    void restoreSupportsNestedScopes() {
        operator.put(authContext(1L));
        ContextSnapshot second = snapshotWith(authContext(2L));
        ContextSnapshot third = snapshotWith(authContext(3L));

        try (ContextScope ignored = propagator.restore(second)) {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);

            try (ContextScope ignoredNested = propagator.restore(third)) {
                assertThat(holder.get(AuthContext.class))
                        .map(AuthContext::getUserId)
                        .contains(3L);
            }

            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);
        }

        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(1L);
    }

    @Test
    void restoreClearsContextWhenNoPreviousContextExists() {
        ContextSnapshot snapshot = snapshotWith(authContext(2L));

        try (ContextScope ignored = propagator.restore(snapshot)) {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(2L);
        }

        assertThat(holder.get(AuthContext.class)).isEmpty();
    }

    @Test
    void restoredContainerDoesNotMutateSnapshot() {
        ContextSnapshot snapshot = snapshotWith(authContext(1L));

        try (ContextScope ignored = propagator.restore(snapshot)) {
            operator.put(authContext(2L));
        }

        try (ContextScope ignored = propagator.restore(snapshot)) {
            assertThat(holder.get(AuthContext.class))
                    .map(AuthContext::getUserId)
                    .contains(1L);
        }
    }

    @Test
    void containerUsesContextTypeAsStorageKey() {
        operator.put(new CustomAuthContext(1L, "user-1"));

        assertThat(holder.get(AuthContext.class))
                .map(AuthContext::getUserId)
                .contains(1L);
    }

    private static ContextSnapshot snapshotWith(AuthContext authContext) {
        ContextHolder holder = new ContextHolder();
        new ContextHolderOperator(holder).put(authContext);
        return new DefaultContextPropagator(holder).capture();
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

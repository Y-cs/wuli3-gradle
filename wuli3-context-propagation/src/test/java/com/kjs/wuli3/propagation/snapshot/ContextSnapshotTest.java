package com.kjs.wuli3.propagation.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import org.junit.jupiter.api.Test;

class ContextSnapshotTest {

    @Test
    void getReturnsEmptyWhenTypeIsAbsent() {
        final ContextSnapshot snapshot = ContextSnapshot.of(new AuthContext(7L, "alice"));

        assertThat(snapshot.get(InvocationContext.class)).isEmpty();
    }

    @Test
    void sameContextTypeUsesLastValueAndValuesCannotBeMutated() {
        final ContextSnapshot snapshot =
                ContextSnapshot.of(new AuthContext(7L, "alice"), new AuthContext(8L, "bob"));

        assertThat(snapshot.get(AuthContext.class)).contains(new AuthContext(8L, "bob"));
        assertThatThrownBy(snapshot.values()::clear).isInstanceOf(UnsupportedOperationException.class);
    }
}

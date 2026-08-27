package com.kjs.wuli3.propagation.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import org.junit.jupiter.api.Test;

class ContextSnapshotTest {

    @Test
    void getReturnsEmptyWhenTypeIsAbsent() {
        final ContextSnapshot snapshot = ContextSnapshot.of(new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));

        assertThat(snapshot.get(InvocationContext.class)).isEmpty();
    }

    @Test
    void sameContextTypeUsesLastValueAndValuesCannotBeMutated() {
        final ContextSnapshot snapshot = ContextSnapshot.of(
                new AuthContext(PrincipalType.CUSTOMER, "7", "alice"),
                new AuthContext(PrincipalType.ADMIN, "8", "bob"));

        assertThat(snapshot.get(AuthContext.class)).contains(new AuthContext(PrincipalType.ADMIN, "8", "bob"));
        assertThatThrownBy(snapshot.values()::clear).isInstanceOf(UnsupportedOperationException.class);
    }
}

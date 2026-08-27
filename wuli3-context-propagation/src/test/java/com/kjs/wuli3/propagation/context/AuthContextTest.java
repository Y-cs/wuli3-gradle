package com.kjs.wuli3.propagation.context;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AuthContextTest {

    @Test
    @SuppressWarnings("NullAway")
    void rejectsNullFields() {
        assertThatNullPointerException().isThrownBy(() -> new AuthContext(null, "7", "alice"));
        assertThatNullPointerException().isThrownBy(() -> new AuthContext(PrincipalType.CUSTOMER, null, "alice"));
        assertThatNullPointerException().isThrownBy(() -> new AuthContext(PrincipalType.CUSTOMER, "7", null));
    }

    @Test
    void rejectsBlankTextFields() {
        assertThatThrownBy(() -> new AuthContext(PrincipalType.CUSTOMER, " ", "alice"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AuthContext(PrincipalType.CUSTOMER, "7", "\t"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package com.kjs.wuli3.propagation.encoding;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthContextEncoderTest {

    @Test
    void writesAuthenticationFields() {
        final Map<String, String> fields = new LinkedHashMap<>();

        AuthContextEncoder.writeTo(new AuthContext(7L, "alice"), fields::put);

        assertThat(fields)
                .containsEntry(AuthContextEncoder.USER_ID, "7")
                .containsEntry(AuthContextEncoder.USERNAME, "alice");
    }
}

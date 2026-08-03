package com.kjs.wuli3.propagation.encoding;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.InvocationContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InvocationContextEncoderTest {

    @Test
    void writesInvocationFields() {
        final Map<String, String> fields = new LinkedHashMap<>();

        InvocationContextEncoder.writeTo(new InvocationContext("10.0.0.8", "request-42"), fields::put);

        assertThat(fields)
                .containsEntry(InvocationContextEncoder.REQUEST_ID, "request-42")
                .containsEntry(InvocationContextEncoder.ORIGIN_IP, "10.0.0.8");
    }
}

package com.kjs.wuli3.opentelemetry.trace;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TraceContextTest {

    @Test
    @SuppressWarnings("NullAway")
    void rejectsNullFields() {
        assertThatNullPointerException().isThrownBy(() -> new TraceContext(null, "span"));
        assertThatNullPointerException().isThrownBy(() -> new TraceContext("trace", null));
    }

    @Test
    void rejectsBlankFields() {
        assertThatThrownBy(() -> new TraceContext(" ", "span")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TraceContext("trace", "\t")).isInstanceOf(IllegalArgumentException.class);
    }
}

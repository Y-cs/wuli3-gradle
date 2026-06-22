package com.kjs.wuli3.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JsonSupportTest {
    @Test
    void serializesJavaTimeAsText() {
        String json = JsonSupport.toJson(new Sample("demo", LocalDateTime.of(2026, 6, 22, 10, 30)));

        assertThat(json).contains("\"time\":\"2026-06-22T10:30:00\"");
    }

    @Test
    void deserializesJson() {
        Sample sample = JsonSupport.fromJson("{\"name\":\"demo\",\"time\":\"2026-06-22T10:30:00\"}", Sample.class);

        assertThat(sample.name()).isEqualTo("demo");
        assertThat(sample.time()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 30));
    }

    record Sample(String name, LocalDateTime time) {
    }
}

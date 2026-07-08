package com.kjs.wuli3.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JacksonProviderTest {
    @Test
    void defaultObjectMapperSerializesJavaTimeAsText() throws Exception {
        final String json = JacksonProvider.defaultObjectMapper()
                .writeValueAsString(new Sample("demo", LocalDateTime.of(2026, 6, 22, 10, 30)));

        assertThat(json).contains("\"time\":\"2026-06-22T10:30:00\"");
    }

    @Test
    void createObjectMapperReturnsIndependentProjectMapper() throws Exception {
        final JsonMapper first = JacksonProvider.createObjectMapper();
        final JsonMapper second = JacksonProvider.createObjectMapper();

        assertThat(first).isNotSameAs(second);
        assertThat(first.writeValueAsString(new Sample("demo", LocalDateTime.of(2026, 6, 22, 10, 30))))
                .contains("\"time\":\"2026-06-22T10:30:00\"");
    }

    @Test
    void defaultObjectMapperIsShared() {
        final ObjectMapper first = JacksonProvider.defaultObjectMapper();
        final ObjectMapper second = JacksonProvider.defaultObjectMapper();

        assertThat(first).isSameAs(second);
    }

    record Sample(String name, LocalDateTime time) {}
}

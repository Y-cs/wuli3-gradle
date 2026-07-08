package com.kjs.wuli3.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.core.error.ErrorCodeException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonsTest {
    @Test
    void serializesJavaTimeAsText() {
        final String json = Jsons.toJson(new Sample("demo", LocalDateTime.of(2026, 6, 22, 10, 30)));

        assertThat(json).contains("\"time\":\"2026-06-22T10:30:00\"");
    }

    @Test
    void deserializesJson() {
        final Sample sample = Jsons.fromJson("{\"name\":\"demo\",\"time\":\"2026-06-22T10:30:00\"}", Sample.class);

        assertThat(sample.name()).isEqualTo("demo");
        assertThat(sample.time()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 30));
    }

    @Test
    void deserializesParameterizedJson() {
        final TypeReference<List<Sample>> typeReference = new TypeReference<>() {};
        final List<Sample> samples =
                Jsons.fromJson("[{\"name\":\"demo\",\"time\":\"2026-06-22T10:30:00\"}]", typeReference);

        assertThat(samples)
                .singleElement()
                .satisfies(sample -> assertThat(sample.name()).isEqualTo("demo"));
    }

    @Test
    void serializesAndDeserializesBytes() {
        final byte[] json = Jsons.toJsonBytes(new Sample("demo", LocalDateTime.of(2026, 6, 22, 10, 30)));
        final Sample sample = Jsons.fromJsonBytes(json, Sample.class);

        assertThat(sample.name()).isEqualTo("demo");
        assertThat(sample.time()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 30));
    }

    @Test
    void deserializesParameterizedBytes() {
        final byte[] json = Jsons.toJsonBytes(List.of(new Sample("demo", LocalDateTime.of(2026, 6, 22, 10, 30))));
        final TypeReference<List<Sample>> typeReference = new TypeReference<>() {};
        final List<Sample> samples = Jsons.fromJsonBytes(json, typeReference);

        assertThat(samples)
                .singleElement()
                .satisfies(sample -> assertThat(sample.time()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 30)));
    }

    @Test
    void mapsDeserializeFailureToJsonError() {
        assertThatThrownBy(() -> Jsons.fromJson("{", Sample.class))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(JsonErrors.DESERIALIZATION_FAILED));
    }

    @Test
    void executesCustomJsonFunctionWithJsonErrorMapping() {
        assertThatThrownBy(() -> Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> {
                    throw new IOException("Cannot read JSON stream");
                }))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(JsonErrors.DESERIALIZATION_FAILED));
    }

    record Sample(String name, LocalDateTime time) {}
}

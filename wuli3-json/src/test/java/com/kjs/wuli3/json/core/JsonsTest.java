package com.kjs.wuli3.json.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import com.kjs.wuli3.core.error.model.ErrorVisibility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonsTest {
    @Test
    void serializesJavaTimeWithProjectFormats() {
        final String json = Jsons.toJson(Sample.create());

        assertThat(json)
                .contains("\"date\":\"2026-06-22\"")
                .contains("\"time\":\"10:30:05\"")
                .contains("\"dateTime\":\"2026-06-22 10:30:05\"");
    }

    @Test
    void deserializesJson() {
        final Sample sample = Jsons.fromJson(
                "{\"name\":\"demo\",\"date\":\"2026-06-22\",\"time\":\"10:30:05\","
                        + "\"dateTime\":\"2026-06-22 10:30:05\"}",
                Sample.class);

        assertThat(sample).isEqualTo(Sample.create());
    }

    @Test
    void deserializesParameterizedJson() {
        final TypeReference<List<Sample>> typeReference = new TypeReference<>() {};
        final List<Sample> samples = Jsons.fromJson(
                "[{\"name\":\"demo\",\"date\":\"2026-06-22\",\"time\":\"10:30:05\","
                        + "\"dateTime\":\"2026-06-22 10:30:05\"}]",
                typeReference);

        assertThat(samples)
                .singleElement()
                .satisfies(sample -> assertThat(sample).isEqualTo(Sample.create()));
    }

    @Test
    void serializesAndDeserializesBytes() {
        final byte[] json = Jsons.toJsonBytes(Sample.create());
        final Sample sample = Jsons.fromJsonBytes(json, Sample.class);

        assertThat(sample).isEqualTo(Sample.create());
    }

    @Test
    void deserializesParameterizedBytes() {
        final byte[] json = Jsons.toJsonBytes(List.of(Sample.create()));
        final TypeReference<List<Sample>> typeReference = new TypeReference<>() {};
        final List<Sample> samples = Jsons.fromJsonBytes(json, typeReference);

        assertThat(samples)
                .singleElement()
                .satisfies(sample -> assertThat(sample).isEqualTo(Sample.create()));
    }

    @Test
    void mapsDeserializeFailureToJsonError() {
        assertThatThrownBy(() -> Jsons.fromJson("{", Sample.class))
                .isInstanceOfSatisfying(ErrorCodeException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(JsonErrors.DESERIALIZATION_FAILED);
                    assertThat(ex.getOrigin()).isEqualTo(ErrorOrigin.SERVER);
                    assertThat(ex.getSeverity()).isEqualTo(ErrorSeverity.CRITICAL);
                    assertThat(ex.getVisibility()).isEqualTo(ErrorVisibility.INTERNAL);
                });
    }

    record Sample(String name, LocalDate date, LocalTime time, LocalDateTime dateTime) {
        static Sample create() {
            final LocalDate date = LocalDate.of(2026, 6, 22);
            final LocalTime time = LocalTime.of(10, 30, 5);
            return new Sample("demo", date, time, LocalDateTime.of(date, time));
        }
    }
}

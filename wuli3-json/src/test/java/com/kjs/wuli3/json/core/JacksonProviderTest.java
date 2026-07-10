package com.kjs.wuli3.json.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.kjs.wuli3.json.provider.JacksonProvider;
import com.kjs.wuli3.json.provider.JsonMapperFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class JacksonProviderTest {
    @Test
    void defaultJsonMapperSerializesJavaTimeWithProjectFormats() throws Exception {
        final String json = JacksonProvider.defaultJsonMapper().writeValueAsString(Sample.create());

        assertThat(json)
                .contains("\"date\":\"2026-06-22\"")
                .contains("\"time\":\"10:30:05\"")
                .contains("\"dateTime\":\"2026-06-22 10:30:05\"");
    }

    @Test
    void defaultJsonMapperDeserializesJavaTimeWithProjectFormats() throws Exception {
        final Sample sample = JacksonProvider.defaultJsonMapper().readValue("""
                        {"name":"demo","date":"2026-06-22","time":"10:30:05","dateTime":"2026-06-22 10:30:05"}
                        """, Sample.class);

        assertThat(sample).isEqualTo(Sample.create());
    }

    @Test
    void defaultJsonMapperMapsUnknownEnumValuesToNull() throws Exception {
        final EnumSample sample = JacksonProvider.defaultJsonMapper().readValue("""
                        {"status":"REMOVED"}
                        """, EnumSample.class);

        assertThat(sample.status()).isNull();
    }

    @Test
    void defaultJsonMapperHonorsJacksonAnnotations() throws Exception {
        final String json = JacksonProvider.defaultJsonMapper().writeValueAsString(new AnnotationSample("demo"));

        assertThat(json).contains("\"alias\":\"demo\"");
    }

    @Test
    void standardJsonMapperFactoryReturnsIndependentProjectMappers() throws Exception {
        final JsonMapper first = JsonMapperFactory.standardJsonMapperFactory().create();
        final JsonMapper second = JsonMapperFactory.standardJsonMapperFactory().create();

        assertThat(first).isNotSameAs(second);
        assertThat(first.writeValueAsString(Sample.create())).contains("\"dateTime\":\"2026-06-22 10:30:05\"");
    }

    @Test
    void featureExportsIncludeSerializationAndDeserializationConfigs() {
        assertThat(JacksonProvider.featuresToEnable())
                .contains(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertThat(JacksonProvider.featuresToDisabled())
                .contains(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        SerializationFeature.FAIL_ON_EMPTY_BEANS,
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void defaultJsonMapperIsShared() {
        final ObjectMapper first = JacksonProvider.defaultJsonMapper();
        final ObjectMapper second = JacksonProvider.defaultJsonMapper();

        assertThat(first).isSameAs(second);
    }

    record Sample(String name, LocalDate date, LocalTime time, LocalDateTime dateTime) {
        static Sample create() {
            final LocalDate date = LocalDate.of(2026, 6, 22);
            final LocalTime time = LocalTime.of(10, 30, 5);
            return new Sample("demo", date, time, LocalDateTime.of(date, time));
        }
    }

    record EnumSample(Status status) {}

    enum Status {
        ENABLED
    }

    static final class AnnotationSample {
        @JsonProperty("alias")
        public String name = "";

        AnnotationSample() {}

        AnnotationSample(final String name) {
            this.name = name;
        }
    }
}

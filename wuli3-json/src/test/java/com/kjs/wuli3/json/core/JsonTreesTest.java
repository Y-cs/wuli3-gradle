package com.kjs.wuli3.json.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonTreesTest {
    @Test
    void readsJsonTree() {
        final JsonNode node = JsonTrees.readTree("{\"name\":\"demo\",\"enabled\":true}");

        assertThat(node.path("name").asText()).isEqualTo("demo");
        assertThat(node.path("enabled").asBoolean()).isTrue();
    }

    @Test
    void createsObjectAndArrayNodes() {
        final ObjectNode node = JsonTrees.createObjectNode();
        node.put("name", "demo");
        node.set("items", JsonTrees.createArrayNode().add("first"));

        final String json = Jsons.toJson(node);

        assertThat(json).contains("\"name\":\"demo\"");
        assertThat(json).contains("\"items\":[\"first\"]");
    }

    @Test
    void convertsBetweenValueAndTree() {
        final Sample source = Sample.create();
        final JsonNode node = JsonTrees.valueToTree(source);
        final Sample sample = JsonTrees.treeToValue(node, Sample.class);

        assertThat(node.path("date").asText()).isEqualTo("2026-06-22");
        assertThat(node.path("time").asText()).isEqualTo("10:30:05");
        assertThat(node.path("dateTime").asText()).isEqualTo("2026-06-22 10:30:05");
        assertThat(sample).isEqualTo(source);
    }

    @Test
    void convertsTreeToParameterizedValue() {
        final JsonNode node = JsonTrees.readTree("[{\"name\":\"demo\",\"date\":\"2026-06-22\",\"time\":\"10:30:05\","
                + "\"dateTime\":\"2026-06-22 10:30:05\"}]");
        final TypeReference<List<Sample>> typeReference = new TypeReference<>() {};
        final List<Sample> samples = JsonTrees.treeToValue(node, typeReference);

        assertThat(samples)
                .singleElement()
                .satisfies(sample -> assertThat(sample).isEqualTo(Sample.create()));
    }

    @Test
    void mapsTreeReadFailureToJsonError() {
        assertThatThrownBy(() -> JsonTrees.readTree("{"))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(JsonErrors.DESERIALIZATION_FAILED));
    }

    record Sample(String name, LocalDate date, LocalTime time, LocalDateTime dateTime) {
        static Sample create() {
            final LocalDate date = LocalDate.of(2026, 6, 22);
            final LocalTime time = LocalTime.of(10, 30, 5);
            return new Sample("demo", date, time, LocalDateTime.of(date, time));
        }
    }
}

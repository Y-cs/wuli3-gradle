package com.kjs.wuli3.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import java.time.LocalDateTime;
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
        final Sample source = new Sample("demo", LocalDateTime.of(2026, 6, 22, 10, 30));
        final JsonNode node = JsonTrees.valueToTree(source);
        final Sample sample = JsonTrees.treeToValue(node, Sample.class);

        assertThat(node.path("time").asText()).isEqualTo("2026-06-22T10:30:00");
        assertThat(sample).isEqualTo(source);
    }

    @Test
    void convertsTreeToParameterizedValue() {
        final JsonNode node = JsonTrees.readTree("[{\"name\":\"demo\",\"time\":\"2026-06-22T10:30:00\"}]");
        final TypeReference<List<Sample>> typeReference = new TypeReference<>() {};
        final List<Sample> samples = JsonTrees.treeToValue(node, typeReference);

        assertThat(samples)
                .singleElement()
                .satisfies(sample -> assertThat(sample.name()).isEqualTo("demo"));
    }

    @Test
    void mapsTreeReadFailureToJsonError() {
        assertThatThrownBy(() -> JsonTrees.readTree("{"))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(JsonErrors.DESERIALIZATION_FAILED));
    }

    record Sample(String name, LocalDateTime time) {}
}

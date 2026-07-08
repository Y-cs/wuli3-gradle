package com.kjs.wuli3.json;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON tree utilities for code that needs Jackson tree-model access instead of direct value binding.
 */
public final class JsonTrees {
    private JsonTrees() {}

    public static JsonNode readTree(final String json) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readTree(json));
    }

    public static JsonNode readTree(final byte[] json) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readTree(json));
    }

    public static ObjectNode createObjectNode() {
        return JacksonProvider.defaultObjectMapper().createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return JacksonProvider.defaultObjectMapper().createArrayNode();
    }

    public static JsonNode valueToTree(final Object value) {
        return Jsons.execute(JsonErrors.SERIALIZATION_FAILED, objectMapper -> objectMapper.valueToTree(value));
    }

    public static <T> T treeToValue(final TreeNode node, final Class<T> type) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.treeToValue(node, type));
    }

    public static <T> T treeToValue(final TreeNode node, final TypeReference<T> typeReference) {
        return Jsons.execute(
                JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.treeToValue(node, typeReference));
    }
}

package com.kjs.wuli3.json.core;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.Nullable;

/**
 * 面向 Jackson 树模型的 JSON 工具。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class JsonTrees {
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    private JsonTrees() {}

    /** 读取 JSON 树；输入无内容时返回 Jackson 的缺失节点。 */
    public static JsonNode readTree(final String json) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readTree(json));
    }

    /** 读取 JSON 字节树；输入无内容时返回 Jackson 的缺失节点。 */
    public static JsonNode readTree(final byte[] json) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readTree(json));
    }

    public static ObjectNode createObjectNode() {
        return JsonTrees.NODE_FACTORY.objectNode();
    }

    public static ArrayNode createArrayNode() {
        return JsonTrees.NODE_FACTORY.arrayNode();
    }

    /** 将 Java 值转换为 JSON 树；Java {@code null} 会转换为 Jackson 的空节点。 */
    public static JsonNode valueToTree(final Object value) {
        return Jsons.execute(JsonErrors.SERIALIZATION_FAILED, objectMapper -> objectMapper.valueToTree(value));
    }

    /** 将 JSON 树转换为指定类型；树表示空值时返回 {@code null}。 */
    public static <T> @Nullable T treeToValue(final TreeNode node, final Class<T> type) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.treeToValue(node, type));
    }

    /** 将 JSON 树按泛型类型转换；树表示空值时返回 {@code null}。 */
    public static <T> @Nullable T treeToValue(final TreeNode node, final TypeReference<T> typeReference) {
        return Jsons.execute(
                JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.treeToValue(node, typeReference));
    }
}

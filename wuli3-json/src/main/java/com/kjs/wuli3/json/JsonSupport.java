package com.kjs.wuli3.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;

public final class JsonSupport {
    private static final ObjectMapper OBJECT_MAPPER = JacksonProvider.defaultObjectMapper();

    private JsonSupport() {}

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw jsonException("Failed to serialize object to JSON", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw jsonException("Failed to deserialize JSON", ex);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw jsonException("Failed to deserialize JSON", ex);
        }
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    private static ErrorCodeException jsonException(String message, JsonProcessingException ex) {
        return new ErrorCodeException(SystemErrors.ILLEGAL_STATE, message, ex);
    }
}

package com.kjs.wuli3.json.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.json.provider.JacksonProvider;

/**
 * Project-level JSON utility facade backed by the standard Jackson {@link ObjectMapper}.
 */
public final class Jsons {
    private static final ObjectMapper OBJECT_MAPPER = JacksonProvider.newJsonMapper();

    private Jsons() {}

    public static String toJson(final Object value) {
        return Jsons.execute(JsonErrors.SERIALIZATION_FAILED, objectMapper -> objectMapper.writeValueAsString(value));
    }

    public static byte[] toJsonBytes(final Object value) {
        return Jsons.execute(JsonErrors.SERIALIZATION_FAILED, objectMapper -> objectMapper.writeValueAsBytes(value));
    }

    public static <T> T fromJson(final String json, final Class<T> type) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readValue(json, type));
    }

    public static <T> T fromJson(final String json, final TypeReference<T> typeReference) {
        return Jsons.execute(
                JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readValue(json, typeReference));
    }

    public static <T> T fromJsonBytes(final byte[] json, final Class<T> type) {
        return Jsons.execute(JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readValue(json, type));
    }

    public static <T> T fromJsonBytes(final byte[] json, final TypeReference<T> typeReference) {
        return Jsons.execute(
                JsonErrors.DESERIALIZATION_FAILED, objectMapper -> objectMapper.readValue(json, typeReference));
    }

    /**
     * Executes a custom Jackson operation and maps checked failures to the project JSON error model.
     */
    static <T> T execute(final JsonErrors error, final JsonFunction<T> function) {
        try {
            return function.apply(Jsons.OBJECT_MAPPER);
        } catch (ErrorCodeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw Jsons.jsonException(error, ex);
        }
    }

    private static ErrorCodeException jsonException(final JsonErrors error, final Exception ex) {
        return new ErrorCodeException(error, error.getMessage(), ex);
    }
}

package com.kjs.wuli3.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Provides the project-standard Jackson configuration used by infrastructure modules and JSON utilities.
 */
public final class JacksonProvider {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = JacksonProvider.createObjectMapper();

    private JacksonProvider() {}

    public static ObjectMapper defaultObjectMapper() {
        return JacksonProvider.DEFAULT_OBJECT_MAPPER;
    }

    public static JsonFactory jsonFactory() {
        return JacksonProvider.DEFAULT_OBJECT_MAPPER.getFactory();
    }

    public static JsonMapper createObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}

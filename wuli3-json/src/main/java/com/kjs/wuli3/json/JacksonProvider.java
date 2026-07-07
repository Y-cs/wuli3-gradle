package com.kjs.wuli3.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonProvider {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = createObjectMapper();

    private JacksonProvider() {}

    public static JavaTimeModule create() {
        return new JavaTimeModule();
    }

    public static ObjectMapper defaultObjectMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }

    public static JsonMapper createObjectMapper() {
        return JsonMapper.builder()
                .addModule(create())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}

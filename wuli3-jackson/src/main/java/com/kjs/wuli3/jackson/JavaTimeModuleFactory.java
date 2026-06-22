package com.kjs.wuli3.jackson;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JavaTimeModuleFactory {
    private JavaTimeModuleFactory() {
    }

    public static JavaTimeModule create() {
        return new JavaTimeModule();
    }

    public static JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .addModule(create())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}

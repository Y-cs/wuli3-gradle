package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.cfg.ConfigFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

/**
 * Provides the project-standard Jackson configuration used by infrastructure modules and JSON utilities.
 */
public final class JacksonProvider {

    private static final JsonMapper DEFAULT_OBJECT_MAPPER = JacksonProvider.createDefaultJsonMapper();
    private static final JsonMapperBaseAssembly JSON_MAPPER_BASE_ASSEMBLY = new JsonMapperBaseAssembly();
    private static final JsonMapperTimeAssembly JSON_MAPPER_TIME_ASSEMBLY = new JsonMapperTimeAssembly();
    private static final JsonMapperEnumAssembly JSON_MAPPER_ENUM_ASSEMBLY = new JsonMapperEnumAssembly();

    private JacksonProvider() {}

    private static JsonMapper createDefaultJsonMapper() {
        return JsonMapperFactory.standardJsonMapperFactory()
                .create();
    }

    public static JsonMapper defaultJsonMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }

    public static JavaTimeModule javaTimeModule() {
        return JSON_MAPPER_TIME_ASSEMBLY.javaTimeModule();
    }

    public static JsonMapperBaseAssembly getJsonMapperBaseAssembly() {
        return JSON_MAPPER_BASE_ASSEMBLY;
    }

    public static JsonMapperTimeAssembly getJsonMapperTimeAssembly() {
        return JSON_MAPPER_TIME_ASSEMBLY;
    }

    public static JsonMapperEnumAssembly getJsonMapperEnumAssembly() {
        return JSON_MAPPER_ENUM_ASSEMBLY;
    }

    public static Locale defaultLocale() {
        return JSON_MAPPER_TIME_ASSEMBLY.defaultLocale();
    }

    public static TimeZone defaultTimeZone() {
        return JSON_MAPPER_TIME_ASSEMBLY.defaultTimeZone();
    }

    public static ConfigFeature[] featuresToEnable() {
        return Stream.of(JSON_MAPPER_BASE_ASSEMBLY, JSON_MAPPER_TIME_ASSEMBLY, JSON_MAPPER_ENUM_ASSEMBLY)
                .map(JsonMapperAssemblyChain::deserializationConfigs)
                .flatMap(map -> map.entrySet()
                        .stream())
                .filter(e -> e.getValue() == FeatureState.ENABLED)
                .map(Map.Entry::getKey)
                .toArray(ConfigFeature[]::new);
    }

    public static ConfigFeature[] featuresToDisabled() {
        return Stream.of(JSON_MAPPER_BASE_ASSEMBLY, JSON_MAPPER_TIME_ASSEMBLY, JSON_MAPPER_ENUM_ASSEMBLY)
                .map(JsonMapperAssemblyChain::deserializationConfigs)
                .flatMap(map -> map.entrySet()
                        .stream())
                .filter(e -> e.getValue() == FeatureState.DISABLED)
                .map(Map.Entry::getKey)
                .toArray(ConfigFeature[]::new);
    }

}

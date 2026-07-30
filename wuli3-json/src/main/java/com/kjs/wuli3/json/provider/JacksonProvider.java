package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.cfg.ConfigFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

/**
 * Provides the project-standard Jackson configuration used by infrastructure modules and JSON utilities.
 */
public final class JacksonProvider {

    private static final JsonMapperBaseAssembly JSON_MAPPER_BASE_ASSEMBLY = new JsonMapperBaseAssembly();
    private static final JsonMapperTimeAssembly JSON_MAPPER_TIME_ASSEMBLY = new JsonMapperTimeAssembly();
    private static final JsonMapperEnumAssembly JSON_MAPPER_ENUM_ASSEMBLY = new JsonMapperEnumAssembly();

    private JacksonProvider() {}

    /** Creates an independently owned mapper with the project-standard configuration. */
    public static JsonMapper newJsonMapper() {
        return JsonMapperFactory.standardJsonMapperFactory().create();
    }

    public static JavaTimeModule javaTimeModule() {
        return JSON_MAPPER_TIME_ASSEMBLY.javaTimeModule();
    }

    /**
     * 为容器管理且采用增量配置的映射器创建具有唯一标识的时间模块。
     *
     * @return 在容器默认配置后应用项目日期时间格式的模块
     */
    public static SimpleModule javaTimeOverrideModule() {
        return JSON_MAPPER_TIME_ASSEMBLY.javaTimeOverrideModule();
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
                .flatMap(JacksonProvider::configEntries)
                .filter(e -> e.getValue() == FeatureState.ENABLED)
                .map(Map.Entry::getKey)
                .toArray(ConfigFeature[]::new);
    }

    public static ConfigFeature[] featuresToDisabled() {
        return Stream.of(JSON_MAPPER_BASE_ASSEMBLY, JSON_MAPPER_TIME_ASSEMBLY, JSON_MAPPER_ENUM_ASSEMBLY)
                .flatMap(JacksonProvider::configEntries)
                .filter(e -> e.getValue() == FeatureState.DISABLED)
                .map(Map.Entry::getKey)
                .toArray(ConfigFeature[]::new);
    }

    private static Stream<Map.Entry<? extends ConfigFeature, FeatureState>> configEntries(
            final JsonMapperAssemblyChain assemblyChain) {
        return Stream.concat(
                assemblyChain.deserializationConfigs().entrySet().stream(),
                assemblyChain.serializationConfigs().entrySet().stream());
    }
}

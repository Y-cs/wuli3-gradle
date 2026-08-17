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
 * 提供基础设施模块和 JSON 工具使用的项目标准 Jackson 配置。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class JacksonProvider {

    private static final JsonMapperBaseAssembly JSON_MAPPER_BASE_ASSEMBLY = new JsonMapperBaseAssembly();
    private static final JsonMapperTimeAssembly JSON_MAPPER_TIME_ASSEMBLY = new JsonMapperTimeAssembly();
    private static final JsonMapperEnumAssembly JSON_MAPPER_ENUM_ASSEMBLY = new JsonMapperEnumAssembly();

    private JacksonProvider() {}

    /** 创建使用项目标准配置且独立持有的映射器。 */
    public static JsonMapper newJsonMapper() {
        return JsonMapperFactory.standardJsonMapperFactory().create();
    }

    /** 返回项目标准的 Java 时间模块。 */
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

    /** 返回项目标准的基础 JSON 映射配置。 */
    public static JsonMapperBaseAssembly getJsonMapperBaseAssembly() {
        return JSON_MAPPER_BASE_ASSEMBLY;
    }

    /** 返回项目标准的时间 JSON 映射配置。 */
    public static JsonMapperTimeAssembly getJsonMapperTimeAssembly() {
        return JSON_MAPPER_TIME_ASSEMBLY;
    }

    /** 返回项目标准的枚举 JSON 映射配置。 */
    public static JsonMapperEnumAssembly getJsonMapperEnumAssembly() {
        return JSON_MAPPER_ENUM_ASSEMBLY;
    }

    /** 返回 JSON 映射器使用的默认区域设置。 */
    public static Locale defaultLocale() {
        return JSON_MAPPER_TIME_ASSEMBLY.defaultLocale();
    }

    /** 返回 JSON 映射器使用的默认时区。 */
    public static TimeZone defaultTimeZone() {
        return JSON_MAPPER_TIME_ASSEMBLY.defaultTimeZone();
    }

    /** 返回需要显式启用的 Jackson 配置项。 */
    public static ConfigFeature[] featuresToEnable() {
        return Stream.of(JSON_MAPPER_BASE_ASSEMBLY, JSON_MAPPER_TIME_ASSEMBLY, JSON_MAPPER_ENUM_ASSEMBLY)
                .flatMap(JacksonProvider::configEntries)
                .filter(e -> e.getValue() == FeatureState.ENABLED)
                .map(Map.Entry::getKey)
                .toArray(ConfigFeature[]::new);
    }

    /** 返回需要显式禁用的 Jackson 配置项。 */
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

package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;

/**
 * 向 {@link JsonMapper.Builder} 应用一项项目级 JSON 能力。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface JsonMapperAssemblyChain {

    /** 向映射器构建器注册模块或其他扩展。 */
    void assemble(JsonMapper.Builder mapperBuilder);

    /** 根据配置映射应用 Jackson 序列化和反序列化特性。 */
    default void featureConfigs(final JsonMapper.Builder mapperBuilder) {
        deserializationConfigs().forEach((feature, state) -> {
            mapperBuilder.configure(feature, state == FeatureState.ENABLED);
        });
        serializationConfigs().forEach((feature, state) -> {
            mapperBuilder.configure(feature, state == FeatureState.ENABLED);
        });
    }

    /** 返回反序列化特性配置；默认不修改配置。 */
    default Map<DeserializationFeature, FeatureState> deserializationConfigs() {
        return Map.of();
    }

    /** 返回序列化特性配置；默认不修改配置。 */
    default Map<SerializationFeature, FeatureState> serializationConfigs() {
        return Map.of();
    }
}

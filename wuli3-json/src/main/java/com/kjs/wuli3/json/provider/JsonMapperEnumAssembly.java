package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;

/**
 * 应用项目级枚举绑定行为。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class JsonMapperEnumAssembly implements JsonMapperAssemblyChain {

    @Override
    public void assemble(final JsonMapper.Builder mapperBuilder) {}

    @Override
    public Map<DeserializationFeature, FeatureState> deserializationConfigs() {
        return Map.of(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, FeatureState.ENABLED);
    }
}

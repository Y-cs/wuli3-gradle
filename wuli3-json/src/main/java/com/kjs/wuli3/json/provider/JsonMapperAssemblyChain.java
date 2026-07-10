package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;

/**
 * Applies one project JSON concern to a {@link JsonMapper.Builder}.
 */
public interface JsonMapperAssemblyChain {

    void assemble(JsonMapper.Builder mapperBuilder);

    default void featureConfigs(JsonMapper.Builder mapperBuilder) {
        deserializationConfigs().forEach((feature, state) -> {
            mapperBuilder.configure(feature, state == FeatureState.ENABLED);
        });
        serializationConfigs().forEach((feature, state) -> {
            mapperBuilder.configure(feature, state == FeatureState.ENABLED);
        });
    }

    default Map<DeserializationFeature, FeatureState> deserializationConfigs() {
        return Map.of();
    }

    default Map<SerializationFeature, FeatureState> serializationConfigs() {
        return Map.of();
    }
}

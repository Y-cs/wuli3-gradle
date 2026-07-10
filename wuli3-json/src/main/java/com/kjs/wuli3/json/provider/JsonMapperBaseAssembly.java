package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Applies baseline lenient JSON binding behavior shared by project mappers.
 */
public final class JsonMapperBaseAssembly implements JsonMapperAssemblyChain {

    @Override
    public void assemble(final JsonMapper.Builder mapperBuilder) {
    }

    @Override
    public Map<DeserializationFeature, FeatureState> deserializationConfigs() {
        return Map.of(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, FeatureState.DISABLED);
    }

    @Override
    public Map<SerializationFeature, FeatureState> serializationConfigs() {
        return Map.of(SerializationFeature.FAIL_ON_EMPTY_BEANS, FeatureState.DISABLED,
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, FeatureState.DISABLED);
    }

}

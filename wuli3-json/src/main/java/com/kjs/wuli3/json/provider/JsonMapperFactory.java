package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Creates independent {@link JsonMapper} instances from an ordered assembly chain.
 */
public final class JsonMapperFactory {

    private final List<JsonMapperAssemblyChain> assemblyChains = new ArrayList<>();

    public JsonMapperFactory addAssemblyChain(final JsonMapperAssemblyChain assemblyChain) {
        this.assemblyChains.add(Objects.requireNonNull(assemblyChain, "assemblyChain"));
        return this;
    }

    public JsonMapper create() {
        final JsonMapper.Builder builder = JsonMapper.builder();
        for (final JsonMapperAssemblyChain assemblyChain : this.assemblyChains) {
            assemblyChain.featureConfigs(builder);
            assemblyChain.assemble(builder);
        }
        return builder.build();
    }

    public static JsonMapperFactory standardJsonMapperFactory() {
        return new JsonMapperFactory()
                .addAssemblyChain(JacksonProvider.getJsonMapperBaseAssembly())
                .addAssemblyChain(JacksonProvider.getJsonMapperTimeAssembly())
                .addAssemblyChain(JacksonProvider.getJsonMapperEnumAssembly());
    }
}

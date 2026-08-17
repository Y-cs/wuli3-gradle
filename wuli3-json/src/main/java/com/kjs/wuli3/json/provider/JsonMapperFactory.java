package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 根据有序装配链创建相互独立的 {@link JsonMapper} 实例。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class JsonMapperFactory {

    private final List<JsonMapperAssemblyChain> assemblyChains = new ArrayList<>();

    /** 添加一项映射器装配链。 */
    public JsonMapperFactory addAssemblyChain(final JsonMapperAssemblyChain assemblyChain) {
        this.assemblyChains.add(Objects.requireNonNull(assemblyChain, "assemblyChain"));
        return this;
    }

    /** 按注册顺序创建映射器。 */
    public JsonMapper create() {
        final JsonMapper.Builder builder = JsonMapper.builder();
        for (final JsonMapperAssemblyChain assemblyChain : this.assemblyChains) {
            assemblyChain.featureConfigs(builder);
            assemblyChain.assemble(builder);
        }
        return builder.build();
    }

    /** 创建包含项目标准装配链的映射器工厂。 */
    public static JsonMapperFactory standardJsonMapperFactory() {
        return new JsonMapperFactory()
                .addAssemblyChain(JacksonProvider.getJsonMapperBaseAssembly())
                .addAssemblyChain(JacksonProvider.getJsonMapperTimeAssembly())
                .addAssemblyChain(JacksonProvider.getJsonMapperEnumAssembly());
    }
}

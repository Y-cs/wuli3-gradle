package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 将 Java 数字序列化为 JSON 字符串，避免客户端处理数值精度时发生丢失。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class JsonMapperNumberStrAssembly implements JsonMapperAssemblyChain {

    private static final SimpleModule MODULE = new SimpleModule();

    static {
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Long.class, ToStringSerializer.instance);
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Long.TYPE, ToStringSerializer.instance);
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Integer.class, ToStringSerializer.instance);
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Integer.TYPE, ToStringSerializer.instance);
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Double.class, ToStringSerializer.instance);
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Double.TYPE, ToStringSerializer.instance);
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Float.class, ToStringSerializer.instance);
        JsonMapperNumberStrAssembly.MODULE.addSerializer(Float.TYPE, ToStringSerializer.instance);
    }

    @Override
    public void assemble(final JsonMapper.Builder mapperBuilder) {
        mapperBuilder
                .addModule(JsonMapperNumberStrAssembly.MODULE)
                .configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS, true);
    }
}

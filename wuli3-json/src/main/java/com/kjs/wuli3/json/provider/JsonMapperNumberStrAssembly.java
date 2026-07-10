package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Serializes Java numbers as JSON strings for clients that cannot safely handle numeric precision.
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

package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Objects;

/**
 * 支持 {@link ResourcePath} 的 Jackson 反序列化器。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ResourcePathJsonDeserializer extends JsonDeserializer<String> {
    private final ResourcePathResolver resolver;
    private final ResourcePath resourcePath;

    public ResourcePathJsonDeserializer(final ResourcePathResolver resolver, final ResourcePath resourcePath) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
    }

    @Override
    public String deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (!p.hasToken(JsonToken.VALUE_STRING)) {
            return (String) ctxt.handleUnexpectedToken(String.class, p);
        }
        if (this.resolver.supports(this.resourcePath.type())) {
            return this.resolver.deserialize(this.resourcePath.type(), p.getValueAsString());
        }
        return p.getValueAsString();
    }
}

package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;
import org.jspecify.annotations.NullUnmarked;

/**
 * Jackson serializer backing {@link ResourcePath}.
 */
public final class ResourcePathJsonSerializer extends JsonSerializer<Object> {
    private final ResourcePath resourcePath;
    private final ResourcePathResolver resolver;

    public ResourcePathJsonSerializer(final ResourcePathResolver resolver, final ResourcePath resourcePath) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
    }

    @NullUnmarked
    @Override
    public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (value instanceof String strValue) {
            if (this.resolver.supports(this.resourcePath.type())) {
                gen.writeString(this.resolver.serialize(this.resourcePath.type(), strValue));
            } else if (this.resourcePath.type().equals("default")) {
                gen.writeString(strValue);
            }
            return;
        }
        throw JsonMappingException.from(gen, "@ResourcePath can only be used on String values.");
    }
}

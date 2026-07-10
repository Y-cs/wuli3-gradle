package com.kjs.wuli3.json.datatype.desensitization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;
import org.jspecify.annotations.NullUnmarked;

/**
 * Jackson serializer backing {@link Desensitized}.
 */
public final class DesensitizedJsonSerializer extends JsonSerializer<Object> {
    private final Desensitized annotation;
    private final DesensitizationStrategyRegistry registry;
    private final DesensitizationVisibilityPolicy visibilityPolicy;

    public DesensitizedJsonSerializer(
            final DesensitizationStrategyRegistry registry,
            final DesensitizationVisibilityPolicy visibilityPolicy,
            final Desensitized annotation) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.visibilityPolicy = Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
        this.annotation = Objects.requireNonNull(annotation, "annotation");
    }

    @NullUnmarked
    @Override
    public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (!(value instanceof String stringValue)) {
            throw JsonMappingException.from(gen, "@Desensitized can only be used on String values.");
        }
        if (this.visibilityPolicy.canViewRaw(this.annotation)) {
            gen.writeString(stringValue);
            return;
        }
        final DesensitizationStrategy strategy = this.registry
                .find(this.annotation.type())
                .orElseThrow(() -> JsonMappingException.from(
                        gen, "No desensitization strategy found for type '" + this.annotation.type() + "'."));
        try {
            gen.writeString(strategy.desensitize(stringValue));
        } catch (RuntimeException ex) {
            throw JsonMappingException.from(
                    gen, "Failed to desensitize JSON value for type '" + this.annotation.type() + "'.", ex);
        }
    }
}

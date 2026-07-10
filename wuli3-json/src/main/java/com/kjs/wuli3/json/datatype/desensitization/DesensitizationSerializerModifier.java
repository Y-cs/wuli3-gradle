package com.kjs.wuli3.json.datatype.desensitization;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.kjs.wuli3.json.datatype.resource.ResourcePath;
import com.kjs.wuli3.json.internal.JacksonAnnotationLookup;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Installs property serializers for fields marked by {@link Desensitized}.
 */
public final class DesensitizationSerializerModifier extends BeanSerializerModifier {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient DesensitizationStrategyRegistry registry;
    private final transient DesensitizationVisibilityPolicy visibilityPolicy;

    public DesensitizationSerializerModifier(
            final DesensitizationStrategyRegistry registry, final DesensitizationVisibilityPolicy visibilityPolicy) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.visibilityPolicy = Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(
            final SerializationConfig config,
            final BeanDescription beanDesc,
            final List<BeanPropertyWriter> beanProperties) {
        for (int index = 0; index < beanProperties.size(); index++) {
            final BeanPropertyWriter writer = beanProperties.get(index);
            final Desensitized annotation = JacksonAnnotationLookup.find(writer, Desensitized.class);
            if (annotation == null) {
                continue;
            }
            final String conflictMessage = DesensitizationSerializerModifier.conflictMessage(writer);
            if (conflictMessage == null) {
                writer.assignSerializer(
                        new DesensitizedJsonSerializer(this.registry, this.visibilityPolicy, annotation));
            } else {
                beanProperties.set(index, new DesensitizationConflictBeanPropertyWriter(writer, conflictMessage));
            }
        }
        return beanProperties;
    }

    private static @Nullable String conflictMessage(final BeanPropertyWriter writer) {
        if (JacksonAnnotationLookup.find(writer, ResourcePath.class) != null) {
            return "@Desensitized cannot be combined with @ResourcePath.";
        }
        if (JacksonAnnotationLookup.find(writer, JsonSerialize.class) != null || writer.getSerializer() != null) {
            return "@Desensitized cannot be combined with another property serializer.";
        }
        return null;
    }
}

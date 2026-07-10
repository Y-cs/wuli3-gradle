package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import java.lang.reflect.AnnotatedElement;
import org.jspecify.annotations.Nullable;

/**
 * Finds {@link ResourcePath} without depending on Jackson's annotation processing switch.
 */
final class ResourcePathAnnotationLookup {
    private ResourcePathAnnotationLookup() {}

    static @Nullable ResourcePath find(final BeanPropertyWriter writer) {
        final ResourcePath annotation = writer.getAnnotation(ResourcePath.class);
        if (annotation != null) {
            return annotation;
        }
        return ResourcePathAnnotationLookup.find(writer.getMember());
    }

    static @Nullable ResourcePath find(final SettableBeanProperty property) {
        final ResourcePath annotation = property.getAnnotation(ResourcePath.class);
        if (annotation != null) {
            return annotation;
        }
        return ResourcePathAnnotationLookup.find(property.getMember());
    }

    private static @Nullable ResourcePath find(final @Nullable AnnotatedMember member) {
        if (member == null) {
            return null;
        }
        final ResourcePath annotation = member.getAnnotation(ResourcePath.class);
        if (annotation != null) {
            return annotation;
        }
        final AnnotatedElement annotatedElement = member.getAnnotated();
        return annotatedElement.getAnnotation(ResourcePath.class);
    }
}

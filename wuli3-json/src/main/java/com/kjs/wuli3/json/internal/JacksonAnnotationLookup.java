package com.kjs.wuli3.json.internal;

import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import org.jspecify.annotations.Nullable;

/**
 * Finds Jackson property annotations across writer/property metadata and the underlying Java element.
 */
public final class JacksonAnnotationLookup {
    private JacksonAnnotationLookup() {}

    public static <A extends Annotation> @Nullable A find(
            final BeanPropertyWriter writer, final Class<A> annotationType) {
        final A annotation = writer.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return JacksonAnnotationLookup.find(writer.getMember(), annotationType);
    }

    public static <A extends Annotation> @Nullable A find(
            final SettableBeanProperty property, final Class<A> annotationType) {
        final A annotation = property.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return JacksonAnnotationLookup.find(property.getMember(), annotationType);
    }

    public static <A extends Annotation> @Nullable A find(
            final @Nullable AnnotatedMember member, final Class<A> annotationType) {
        if (member == null) {
            return null;
        }
        final A annotation = member.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        final AnnotatedElement annotatedElement = member.getAnnotated();
        return annotatedElement.getAnnotation(annotationType);
    }
}

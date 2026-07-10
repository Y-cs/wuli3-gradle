package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a string property as a resource path that needs resolver-based conversion at the JSON boundary.
 */
@JacksonAnnotationsInside
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourcePath {

    /**
     * Resource category understood by the configured {@link ResourcePathResolver}.
     */
    String type() default "default";
}

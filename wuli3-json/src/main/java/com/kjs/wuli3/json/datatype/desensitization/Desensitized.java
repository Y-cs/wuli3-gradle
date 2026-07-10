package com.kjs.wuli3.json.datatype.desensitization;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a string property whose JSON output must be masked according to a stable business semantic.
 */
@JacksonAnnotationsInside
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Desensitized {

    /**
     * Strategy key resolved by {@link DesensitizationStrategyRegistry}.
     */
    String type();
}

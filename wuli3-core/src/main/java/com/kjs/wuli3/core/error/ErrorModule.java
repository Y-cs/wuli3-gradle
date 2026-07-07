package com.kjs.wuli3.core.error;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@NullMarked
public @interface ErrorModule {

    String value();

    ErrorPolicy policy() default @ErrorPolicy;
}

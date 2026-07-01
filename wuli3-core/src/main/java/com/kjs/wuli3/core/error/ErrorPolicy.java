package com.kjs.wuli3.core.error;

import org.jspecify.annotations.NullMarked;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ErrorPolicy
 *
 * @author GuoYang create on 2026/6/26 18:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@NullMarked
public @interface ErrorPolicy {

    ErrorSeverity severity() default ErrorSeverity.NORMAL;

    ErrorVisibility visibility() default ErrorVisibility.PUBLIC;


}

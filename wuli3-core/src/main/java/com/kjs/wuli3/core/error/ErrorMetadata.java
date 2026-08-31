package com.kjs.wuli3.core.error;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

/**
 * 声明错误的固有元数据（责任归属和严重程度）。
 *
 * <p>可以标注在类型（模块默认）或字段（单个错误）上。字段级注解会覆盖模块默认值。
 *
 * @author GuoYang create on 2026/8/28 20:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@NullMarked
public @interface ErrorMetadata {

    /**
     * 错误责任归属。
     */
    ErrorOrigin origin() default ErrorOrigin.CALLER;

    /**
     * 错误严重程度。
     */
    ErrorSeverity severity() default ErrorSeverity.NORMAL;

    /**
     * 错误在边界传播时的可见性。
     *
     * <p>默认 {@link ErrorVisibility#PUBLIC}。
     */
    ErrorVisibility visibility() default ErrorVisibility.PUBLIC;
}

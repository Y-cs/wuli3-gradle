package com.kjs.wuli3.core.error.policy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

/**
 * 声明错误码的责任来源、告警严重度和对外可见性。
 *
 * <p>模块默认策略通过 {@code @ErrorModule.policy()} 声明，枚举常量上的注解可覆盖默认值。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@NullMarked
public @interface ErrorPolicy {

    ErrorSeverity severity() default ErrorSeverity.NORMAL;

    ErrorVisibility visibility() default ErrorVisibility.PUBLIC;

    ErrorOrigin origin() default ErrorOrigin.CALLER;
}

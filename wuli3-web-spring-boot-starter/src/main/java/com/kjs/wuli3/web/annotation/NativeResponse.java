package com.kjs.wuli3.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口返回值需要跳过统一响应包装。
 *
 * <p>可标注在 Controller 类或方法上；方法级配置优先于类级配置。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NativeResponse {

    /**
     * 跳过统一包装的场景。
     */
    NativeResponseMode value() default NativeResponseMode.SUCCESS_ONLY;
}

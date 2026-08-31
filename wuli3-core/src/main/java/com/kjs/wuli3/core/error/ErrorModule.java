package com.kjs.wuli3.core.error;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

/**
 * 声明错误码枚举所属模块及模块默认元数据。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@NullMarked
public @interface ErrorModule {

    /**
     * 模块名称，用于生成错误码前缀。
     */
    String name();

    /** 模块内错误的默认元数据，字段级注解可以覆盖。 */
    ErrorMetadata defaultMetadata() default @ErrorMetadata;
}

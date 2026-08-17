package com.kjs.wuli3.core.error.metadata;

import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

/** 声明错误码枚举所属模块及其默认策略。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@NullMarked
public @interface ErrorModule {

    String value();

    ErrorPolicy policy() default @ErrorPolicy;
}

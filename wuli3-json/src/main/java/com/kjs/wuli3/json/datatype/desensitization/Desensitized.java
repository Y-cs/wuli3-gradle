package com.kjs.wuli3.json.datatype.desensitization;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要按稳定业务语义脱敏后输出到 JSON 的字符串属性。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@JacksonAnnotationsInside
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Desensitized {

    /**
     * 由 {@link DesensitizationStrategyRegistry} 解析的策略键。
     */
    String type();
}

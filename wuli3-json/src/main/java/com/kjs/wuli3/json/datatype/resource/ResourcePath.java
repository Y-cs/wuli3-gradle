package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要在 JSON 边界通过解析器转换的资源路径字符串属性。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@JacksonAnnotationsInside
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourcePath {

    /**
     * 配置的 {@link ResourcePathResolver} 能识别的资源类别。
     */
    String type() default "default";
}

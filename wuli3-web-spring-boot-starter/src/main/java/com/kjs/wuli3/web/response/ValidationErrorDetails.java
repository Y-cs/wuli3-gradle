package com.kjs.wuli3.web.response;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * 统一错误响应中的结构化校验错误详情。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record ValidationErrorDetails(List<Item> errors) {

    public ValidationErrorDetails {
        errors = List.copyOf(errors);
    }

    /**
     * 单个错误项。
     */
    public record Item(@Nullable String field, String code, String message) {}
}

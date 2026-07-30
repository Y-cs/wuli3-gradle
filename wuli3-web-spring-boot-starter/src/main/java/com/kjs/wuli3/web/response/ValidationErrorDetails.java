package com.kjs.wuli3.web.response;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Structured validation details returned in unified error responses.
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

package com.kjs.wuli3.web.response;

import org.jspecify.annotations.Nullable;

import java.util.List;

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
    public record Item(@Nullable String field, String message, @Nullable Object rejectedValue) {
    }
}

package com.kjs.wuli3.web;

import com.kjs.wuli3.core.error.CommonErrorCode;
import com.kjs.wuli3.core.error.ErrorCode;
import org.jspecify.annotations.Nullable;

public record ApiResponse<T>(String code, String message, @Nullable T data, @Nullable String requestId) {
    public static <T> ApiResponse<T> success(T data, @Nullable String requestId) {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.code(), CommonErrorCode.SUCCESS.message(), data, requestId);
    }

    public static ApiResponse<Void> failure(ErrorCode errorCode, String message, @Nullable String requestId) {
        return new ApiResponse<>(errorCode.code(), message, null, requestId);
    }
}

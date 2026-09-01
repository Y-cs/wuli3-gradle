package com.kjs.wuli3.web.response;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.resolver.ErrorCodeResolver;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Web 接口的标准响应包装对象。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record ApiResponse<T>(
        String code,
        String message,
        long timestamp,
        @Nullable String requestId,
        @Nullable T data) {
    public static final String SUCCESS_CODE = "0";

    public ApiResponse {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }

    public static <T> ApiResponse<T> success(final @Nullable T data, final @Nullable String requestId) {
        return ApiResponse.success(data, requestId, "");
    }

    public static <T> ApiResponse<T> success(
            final @Nullable T data, final @Nullable String requestId, final String message) {
        return new ApiResponse<>(SUCCESS_CODE, message, System.currentTimeMillis(), requestId, data);
    }

    public static <T> ApiResponse<T> failure(
            final ErrorCode errorCode,
            final String message,
            final @Nullable String requestId,
            final ErrorCodeResolver errorCodeResolver) {
        return ApiResponse.failure(errorCode, message, requestId, errorCodeResolver, null);
    }

    public static <T> ApiResponse<T> failure(
            final ErrorCode errorCode,
            final String message,
            final @Nullable String requestId,
            final ErrorCodeResolver errorCodeResolver,
            final @Nullable T data) {
        return new ApiResponse<>(
                errorCodeResolver.resolve(errorCode), message, System.currentTimeMillis(), requestId, data);
    }
}

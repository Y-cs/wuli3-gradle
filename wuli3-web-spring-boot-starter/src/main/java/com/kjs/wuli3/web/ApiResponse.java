package com.kjs.wuli3.web;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.web.error.WebErrors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Web 接口的标准响应包装对象。
 */
@ToString
@EqualsAndHashCode
@Getter
public final class ApiResponse<T> {
    private static final String SUCCESS_CODE = "0";

    private String code = SUCCESS_CODE;
    private String message = "";
    private @Nullable T data;
    private @Nullable String requestId;

    private ApiResponse() {
    }

    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    public static <T> ApiResponse<T> success(@Nullable T data, @Nullable String requestId) {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(WebErrors.SUCCESS.getMessage())
                .data(data)
                .requestId(requestId)
                .build();
    }

    public static ApiResponse<Void> failure(
            ErrorCode errorCode,
            String message,
            @Nullable String requestId,
            ErrorCodeResolver errorCodeResolver
    ) {
        return ApiResponse.<Void>builder()
                .code(errorCodeResolver.formatErrorCode(errorCode))
                .message(message)
                .requestId(requestId)
                .build();
    }

    public static class ApiResponseBuilder<T> {
        private final ApiResponse<T> apiResponse = new ApiResponse<>();

        public ApiResponseBuilder<T> code(@Nullable String code) {
            apiResponse.code = Optional.ofNullable(code)
                    .orElse(SUCCESS_CODE);
            return this;
        }

        public ApiResponseBuilder<T> message(@Nullable String message) {
            apiResponse.message = Optional.ofNullable(message)
                    .orElse("");
            return this;
        }

        public ApiResponseBuilder<T> data(@Nullable T data) {
            apiResponse.data = data;
            return this;
        }

        public ApiResponseBuilder<T> requestId(@Nullable String requestId) {
            apiResponse.requestId = requestId;
            return this;
        }

        public ApiResponse<T> build() {
            return apiResponse;
        }
    }

}

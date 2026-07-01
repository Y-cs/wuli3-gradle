package com.kjs.wuli3.web.wrapper;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * Web 接口的标准响应包装对象。
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ApiResponse<T> {
    public static final String SUCCESS_CODE = "0";

    private final String code;
    private final String message;
    private final long timestamp;
    private final @Nullable String requestId;
    private final @Nullable T data;

    public ApiResponse(String code, String message, Long timestamp, @Nullable String requestId, @Nullable T data) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.requestId = requestId;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(@Nullable T data, @Nullable String requestId) {
        return new ApiResponse<>(SUCCESS_CODE, "", System.currentTimeMillis(), requestId, data);
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message, @Nullable String requestId,
            ErrorCodeResolver errorCodeResolver) {
        return new ApiResponse<>(errorCodeResolver.resolver(errorCode), message, System.currentTimeMillis(), requestId,
                null);
    }
}

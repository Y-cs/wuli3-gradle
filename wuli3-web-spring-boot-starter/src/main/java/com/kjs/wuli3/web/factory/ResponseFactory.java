package com.kjs.wuli3.web.factory;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.web.wrapper.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * ResponseFactory
 *
 * @author GuoYang create on 2026/6/26 17:25
 */
@RequiredArgsConstructor
public class ResponseFactory {

    private final InvocationContextAccessor invocationContextAccessor;

    private final ErrorCodeResolver errorCodeResolver;

    public <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data, requestId());
    }

    public <T> ApiResponse<T> success() {
        return ApiResponse.success(null, requestId());
    }

    public <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return ApiResponse.failure(errorCode, message, requestId(), errorCodeResolver);
    }

    private @Nullable String requestId() {
        return invocationContextAccessor.requestId()
                .orElse(null);
    }
}

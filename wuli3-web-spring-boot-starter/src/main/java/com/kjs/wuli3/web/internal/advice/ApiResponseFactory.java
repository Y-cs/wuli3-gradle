package com.kjs.wuli3.web.internal.advice;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.resolver.ErrorCodeResolver;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.web.response.ApiResponse;
import com.kjs.wuli3.web.response.WebResponseProperties;
import org.jspecify.annotations.Nullable;

/**
 * 创建 API 响应，并在可用时附加当前调用的请求 ID。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public class ApiResponseFactory {

    private final InvocationContextAccessor invocationContextAccessor;

    private final ErrorCodeResolver errorCodeResolver;

    private final WebResponseProperties responseProperties;

    public ApiResponseFactory(
            final InvocationContextAccessor invocationContextAccessor, final ErrorCodeResolver errorCodeResolver) {
        this(invocationContextAccessor, errorCodeResolver, new WebResponseProperties());
    }

    public ApiResponseFactory(
            final InvocationContextAccessor invocationContextAccessor,
            final ErrorCodeResolver errorCodeResolver,
            final WebResponseProperties responseProperties) {
        this.invocationContextAccessor = invocationContextAccessor;
        this.errorCodeResolver = errorCodeResolver;
        this.responseProperties = responseProperties;
    }

    /** 创建带数据的成功响应。 */
    public <T> ApiResponse<T> success(final @Nullable T data) {
        return ApiResponse.success(data, this.requestId(), this.responseProperties.getSuccessMessage());
    }

    /** 创建不带数据的成功响应。 */
    public <T> ApiResponse<T> success() {
        return ApiResponse.success(null, this.requestId(), this.responseProperties.getSuccessMessage());
    }

    /** 创建带错误码和消息的失败响应。 */
    public <T> ApiResponse<T> fail(final ErrorCode errorCode, final String message) {
        return ApiResponse.failure(errorCode, message, this.requestId(), this.errorCodeResolver);
    }

    /** 创建带错误码、消息和附加数据的失败响应。 */
    public <T> ApiResponse<T> fail(final ErrorCode errorCode, final String message, final @Nullable T data) {
        return ApiResponse.failure(errorCode, message, this.requestId(), this.errorCodeResolver, data);
    }

    private @Nullable String requestId() {
        return this.invocationContextAccessor.requestId().orElse(null);
    }
}

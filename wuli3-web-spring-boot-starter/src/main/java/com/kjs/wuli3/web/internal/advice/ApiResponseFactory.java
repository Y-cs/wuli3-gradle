package com.kjs.wuli3.web.internal.advice;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier;
import com.kjs.wuli3.core.error.resolver.ErrorResolver;
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

    private final ErrorResolver errorResolver;

    private final WebResponseProperties responseProperties;

    public ApiResponseFactory(
            final InvocationContextAccessor invocationContextAccessor, final ErrorResolver errorResolver) {
        this(invocationContextAccessor, errorResolver, new WebResponseProperties());
    }

    public ApiResponseFactory(
            final InvocationContextAccessor invocationContextAccessor,
            final ErrorResolver errorResolver,
            final WebResponseProperties responseProperties) {
        this.invocationContextAccessor = invocationContextAccessor;
        this.errorResolver = errorResolver;
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
        return ApiResponse.failure(errorCode, message, this.requestId(), this.errorResolver);
    }

    /** 创建带错误码、消息和附加数据的失败响应。 */
    public <T> ApiResponse<T> fail(final ErrorCode errorCode, final String message, final @Nullable T data) {
        return ApiResponse.failure(errorCode, message, this.requestId(), this.errorResolver, data);
    }

    /** 将业务异常按 core 可见性策略解析后创建失败响应。 */
    public <T> ApiResponse<T> fail(final ErrorCodeException exception) {
        final ErrorCodeCarrier carrier = this.errorResolver.resolveBoundary(exception);
        return this.fail(carrier);
    }

    /** 解析异常为统一传播模型，供 HTTP 状态与响应投影共用。 */
    public ErrorCodeCarrier resolve(final ErrorCodeException exception) {
        return this.errorResolver.resolveBoundary(exception);
    }

    /** 将已解析传播模型投影为对外响应，不重复解析。 */
    public <T> ApiResponse<T> fail(final ErrorCodeCarrier carrier) {
        return ApiResponse.failure(carrier, this.requestId(), null);
    }

    private @Nullable String requestId() {
        return this.invocationContextAccessor.requestId().orElse(null);
    }
}

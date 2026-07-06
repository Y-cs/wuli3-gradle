package com.kjs.wuli3.web.internal.response;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.web.config.properties.WebResponseProperties;
import com.kjs.wuli3.web.response.ApiResponse;
import org.jspecify.annotations.Nullable;

/**
 * Creates API responses with the current invocation request id attached when available.
 */
public class ApiResponseFactory {

    private final InvocationContextAccessor invocationContextAccessor;

    private final ErrorCodeResolver errorCodeResolver;

    private final WebResponseProperties responseProperties;

    public ApiResponseFactory(final InvocationContextAccessor invocationContextAccessor, final ErrorCodeResolver errorCodeResolver) {
        this(invocationContextAccessor, errorCodeResolver, new WebResponseProperties());
    }

    public ApiResponseFactory(final InvocationContextAccessor invocationContextAccessor, final ErrorCodeResolver errorCodeResolver,
            final WebResponseProperties responseProperties) {
        this.invocationContextAccessor = invocationContextAccessor;
        this.errorCodeResolver = errorCodeResolver;
        this.responseProperties = responseProperties;
    }

    public <T> ApiResponse<T> success(final @Nullable T data) {
        return ApiResponse.success(data, this.requestId(), this.responseProperties.getSuccessMessage());
    }

    public <T> ApiResponse<T> success() {
        return ApiResponse.success(null, this.requestId(), this.responseProperties.getSuccessMessage());
    }

    public <T> ApiResponse<T> fail(final ErrorCode errorCode, final String message) {
        return ApiResponse.failure(errorCode, message, this.requestId(), this.errorCodeResolver);
    }

    public <T> ApiResponse<T> fail(final ErrorCode errorCode, final String message, final @Nullable T data) {
        return ApiResponse.failure(errorCode, message, this.requestId(), this.errorCodeResolver, data);
    }

    private @Nullable String requestId() {
        return this.invocationContextAccessor.requestId()
                .orElse(null);
    }
}

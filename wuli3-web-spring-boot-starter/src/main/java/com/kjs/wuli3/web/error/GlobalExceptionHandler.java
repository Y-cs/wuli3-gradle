package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.propagation.accessor.RequestContextAccessor;
import com.kjs.wuli3.web.ApiResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 将框架异常和业务异常映射为 {@link com.kjs.wuli3.web.ApiResponse} 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorCodeResolver errorCodeResolver;
    private final RequestContextAccessor requestContextAccessor;

    public GlobalExceptionHandler(
            ErrorCodeResolver errorCodeResolver,
            RequestContextAccessor requestContextAccessor
    ) {
        this.errorCodeResolver = errorCodeResolver;
        this.requestContextAccessor = requestContextAccessor;
    }

    @ExceptionHandler(ErrorCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleErrorCodeException(ErrorCodeException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return buildResponse(status(ex.getErrorPolicy()
                .severity()), errorCode, visibleMessage(ex), visibleErrorCode(ex));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, WebErrors.BAD_REQUEST, WebErrors.BAD_REQUEST.getMessage(),
                WebErrors.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                WebErrors.BAD_REQUEST,
                WebErrors.BAD_REQUEST.getMessage(),
                WebErrors.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                WebErrors.BAD_REQUEST,
                WebErrors.BAD_REQUEST.getMessage(),
                WebErrors.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                WebErrors.INTERNAL_ERROR,
                WebErrors.INTERNAL_ERROR.getMessage(),
                WebErrors.INTERNAL_ERROR
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status,
            ErrorCode fallbackCode,
            String message,
            @Nullable ErrorCode visibleCode
    ) {
        ErrorCode responseCode = visibleCode == null ? fallbackCode : visibleCode;
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(responseCode, message, requestId(), errorCodeResolver));
    }

    private static String visibleMessage(ErrorCodeException ex) {
        ErrorVisibility visibility = ex.getErrorPolicy()
                .visibility();
        if (visibility == ErrorVisibility.CODE_ONLY || visibility == ErrorVisibility.INTERNAL) {
            return WebErrors.INTERNAL_ERROR.getMessage();
        }
        String message = ex.getMessage();
        return message == null ? ex.getErrorCode()
                .getMessage() : message;
    }

    private static @Nullable ErrorCode visibleErrorCode(ErrorCodeException ex) {
        ErrorVisibility visibility = ex.getErrorPolicy()
                .visibility();
        if (visibility == ErrorVisibility.MESSAGE_ONLY || visibility == ErrorVisibility.INTERNAL) {
            return WebErrors.INTERNAL_ERROR;
        }
        return ex.getErrorCode();
    }

    private @Nullable String requestId() {
        return requestContextAccessor.requestId()
                .orElse(null);
    }

    private static HttpStatus status(ErrorSeverity severity) {
        if (severity == ErrorSeverity.CRITICAL || severity == ErrorSeverity.FATAL) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }

}

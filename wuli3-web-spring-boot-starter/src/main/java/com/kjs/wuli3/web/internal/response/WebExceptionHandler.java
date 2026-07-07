package com.kjs.wuli3.web.internal.response;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.web.config.properties.WebResponseProperties;
import com.kjs.wuli3.web.error.ErrorAlertContext;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import com.kjs.wuli3.web.error.WebErrorStatusResolver;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.internal.error.ErrorAlertNotifiers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 将框架异常和业务异常映射为统一响应。
 *
 * <p>这个处理器只负责“HTTP/异常到响应”的边界转换：业务错误码的可见性仍由
 * {@link ErrorCodeException} 的错误策略决定，请求链路信息仍从上下文中读取。
 */
@RestControllerAdvice
public class WebExceptionHandler {

    private final ApiResponseFactory responseFactory;
    private final InvocationContextAccessor invocationContextAccessor;
    private final WebErrorStatusResolver webErrorStatusResolver;
    private final ValidationErrorDetailsFactory validationErrorDetailsFactory;
    private final ErrorAlertNotifiers errorAlertNotifiers;

    public WebExceptionHandler(
            final ApiResponseFactory responseFactory,
            final InvocationContextAccessor invocationContextAccessor,
            final WebResponseProperties responseProperties,
            final List<ErrorAlertNotifier> errorAlertNotifiers,
            final WebErrorStatusResolver webErrorStatusResolver) {
        this.responseFactory = responseFactory;
        this.invocationContextAccessor = invocationContextAccessor;
        this.webErrorStatusResolver = webErrorStatusResolver;
        this.validationErrorDetailsFactory = new ValidationErrorDetailsFactory(responseProperties);
        this.errorAlertNotifiers = new ErrorAlertNotifiers(errorAlertNotifiers);
    }

    @ExceptionHandler(ErrorCodeException.class)
    public ResponseEntity<?> handleErrorCodeException(final ErrorCodeException ex, final HttpServletRequest request) {
        final ErrorCode responseCode = WebErrorResponseMapper.visibleErrorCode(ex);
        final String message = WebErrorResponseMapper.visibleMessage(ex);
        final HttpStatus status = this.webErrorStatusResolver.resolve(ex, responseCode);
        this.alert(ex, request, status, responseCode);
        if (NativeResponseSupport.isAll(request)) {
            // NativeResponseMode.ALL 只跳过 ApiResponse 外壳，错误仍使用 Spring 标准 ProblemDetail。
            return WebErrorResponseMapper.nativeError(
                    status, responseCode, message, this.requestId(), this.responseFactory);
        }
        return ResponseEntity.status(status).body(this.responseFactory.fail(responseCode, message));
    }

    /*
     * 请求本身不合法的异常统一归为 BAD_REQUEST。
     *
     * 参数校验、缺参、类型转换、JSON 解析和不支持的 HTTP 方法/媒体类型都属于调用方可修正的问题，
     * 因此统一走 4xx 响应，但具体 HTTP status 由 status(Exception) 再细分。
     */
    @ExceptionHandler({
        MethodArgumentNotValidException.class, MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class, ServletRequestBindingException.class,
        ConstraintViolationException.class, HttpMessageNotReadableException.class,
        HttpMediaTypeNotSupportedException.class, HttpRequestMethodNotSupportedException.class,
        IllegalArgumentException.class,
    })
    public ResponseEntity<?> handleBadRequest(final Exception ex, final HttpServletRequest request) {
        return this.handleFrameworkException(ex, request);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<?> handleNotFound(final Exception ex, final HttpServletRequest request) {
        return this.handleFrameworkException(
                ex,
                request,
                this.webErrorStatusResolver.resolve(ex, WebErrors.NOT_FOUND),
                WebErrors.NOT_FOUND,
                WebErrors.NOT_FOUND.getMessage(),
                null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(
            final ResponseStatusException ex, final HttpServletRequest request) {
        final HttpStatus status = this.webErrorStatusResolver.resolve(
                ex,
                WebErrorResponseMapper.responseCode(
                        HttpStatus.valueOf(ex.getStatusCode().value())));
        final ErrorCode responseCode = WebErrorResponseMapper.responseCode(status);
        this.alert(ex, request, status, responseCode);
        if (NativeResponseSupport.isAll(request)) {
            return WebErrorResponseMapper.nativeProblemDetail(
                    status.value(), WebErrorResponseMapper.message(ex), this.requestId());
        }
        return ResponseEntity.status(status)
                .body(this.responseFactory.fail(responseCode, WebErrorResponseMapper.message(ex)));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<?> handleErrorResponseException(
            final ErrorResponseException ex, final HttpServletRequest request) {
        final HttpStatus status = this.webErrorStatusResolver.resolve(
                ex,
                WebErrorResponseMapper.responseCode(
                        HttpStatus.valueOf(ex.getStatusCode().value())));
        final ErrorCode responseCode = WebErrorResponseMapper.responseCode(status);
        this.alert(ex, request, status, responseCode);
        if (NativeResponseSupport.isAll(request)) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getBody());
        }
        return ResponseEntity.status(status)
                .body(this.responseFactory.fail(responseCode, WebErrorResponseMapper.message(ex)));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<?> handleMessageNotWritable(
            final HttpMessageNotWritableException ex, final HttpServletRequest request) {
        /*
         * 响应写出失败通常是服务端序列化或消息转换问题，不能向客户端暴露内部异常细节。
         */
        return this.handleFrameworkException(
                ex,
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                WebErrors.INTERNAL_ERROR,
                WebErrors.INTERNAL_ERROR.getMessage(),
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(final Exception ex, final HttpServletRequest request) {
        final HttpStatus status = this.webErrorStatusResolver.resolve(ex, WebErrors.INTERNAL_ERROR);
        final ErrorCode responseCode = WebErrorResponseMapper.responseCode(status);
        this.alert(ex, request, status, responseCode);
        if (NativeResponseSupport.isAll(request)) {
            return WebErrorResponseMapper.nativeProblemDetail(
                    status.value(), responseCode.getMessage(), this.requestId());
        }
        return ResponseEntity.status(status).body(this.responseFactory.fail(responseCode, responseCode.getMessage()));
    }

    private ResponseEntity<?> handleFrameworkException(final Exception ex, final HttpServletRequest request) {
        final HttpStatus status = this.webErrorStatusResolver.resolve(ex, WebErrors.BAD_REQUEST);
        final ErrorCode responseCode = WebErrorResponseMapper.responseCode(status);
        final String message = responseCode == WebErrors.INTERNAL_ERROR
                ? WebErrors.INTERNAL_ERROR.getMessage()
                : responseCode.getMessage();
        return this.handleFrameworkException(
                ex, request, status, responseCode, message, this.validationErrorDetailsFactory.detail(ex));
    }

    private ResponseEntity<?> handleFrameworkException(
            final Throwable error,
            final HttpServletRequest request,
            final HttpStatus status,
            final ErrorCode responseCode,
            final String message,
            final @Nullable Object detail) {
        this.alert(error, request, status, responseCode);
        if (NativeResponseSupport.isAll(request)) {
            /*
             * NativeResponseMode.ALL 表示调用方不想要 ApiResponse 外壳；框架异常仍以 ProblemDetail
             * 输出，避免直接泄漏 servlet 容器的默认错误页或不稳定结构。
             */
            return WebErrorResponseMapper.nativeProblemDetail(status.value(), message, this.requestId());
        }
        return ResponseEntity.status(status).body(this.responseFactory.fail(responseCode, message, detail));
    }

    private void alert(
            final Throwable error,
            final HttpServletRequest request,
            final HttpStatus status,
            final ErrorCode responseCode) {
        this.errorAlertNotifiers.dispatch(new ErrorAlertContext(
                error,
                status,
                responseCode,
                this.requestId(),
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr()));
    }

    private @Nullable String requestId() {
        return this.invocationContextAccessor.requestId().orElse(null);
    }
}

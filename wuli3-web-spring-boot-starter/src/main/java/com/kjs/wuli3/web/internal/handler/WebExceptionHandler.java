package com.kjs.wuli3.web.internal.handler;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.web.error.ErrorAlertContext;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import com.kjs.wuli3.web.error.WebErrorStatusResolver;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.internal.advice.ApiResponseFactory;
import com.kjs.wuli3.web.internal.advice.NativeResponseSupport;
import com.kjs.wuli3.web.internal.advice.ValidationErrorDetailsFactory;
import com.kjs.wuli3.web.internal.error.ErrorAlertNotifiers;
import com.kjs.wuli3.web.internal.error.WebErrorResponseMapper;
import com.kjs.wuli3.web.response.WebResponseProperties;
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
 * <p><strong>核心职责：异常到 HTTP 响应的边界转换</strong></p>
 * <p>本类是 Spring MVC 全局异常处理的入口，负责捕获 Controller 层抛出的异常并转换为统一的
 * {@link com.kjs.wuli3.web.response.ApiResponse} 或 {@link org.springframework.http.ProblemDetail} 响应。
 * 错误可见性过滤委托给 {@link WebErrorResponseMapper}，HTTP 状态码判定委托给 {@link WebErrorStatusResolver}。
 *
 * <h2>业务异常处理（ErrorCodeException）</h2>
 * <p>对于 {@link ErrorCodeException}，处理流程为：
 * <ol>
 *   <li>通过 {@link WebErrorResponseMapper#visibleErrorCode} 和 {@link WebErrorResponseMapper#visibleMessage}
 *       根据 {@link com.kjs.wuli3.core.error.model.ErrorVisibility} 过滤敏感信息</li>
 *   <li>通过 {@link WebErrorStatusResolver#resolve} 根据 {@link com.kjs.wuli3.core.error.model.ErrorOrigin}
 *       决定 HTTP 状态码（CALLER → 400, SERVER → 500）</li>
 *   <li>通过 {@link #shouldAlert} 判断是否需要告警（5xx 或 CRITICAL/FATAL 严重度）</li>
 *   <li>根据 {@code @NativeResponse} 模式返回 {@code ApiResponse} 或 {@code ProblemDetail}</li>
 * </ol>
 *
 * <h2>框架异常处理</h2>
 * <p>Spring MVC 框架异常按类型分组处理：
 * <ul>
 *   <li>参数校验/绑定异常 → 400（可选返回字段级详情）</li>
 *   <li>404/405/415 异常 → 对应状态码</li>
 *   <li>Spring Security 异常 → 401/403（通过 cause 链检测类名）</li>
 *   <li>响应序列化失败 → 500（不暴露内部异常细节）</li>
 *   <li>其他未分类异常 → 500</li>
 * </ul>
 *
 * <h2>告警触发条件</h2>
 * <p>{@link #shouldAlert} 方法判断是否需要通知 {@link com.kjs.wuli3.web.error.ErrorAlertNotifier}：
 * <ul>
 *   <li>任何 5xx 服务端错误都会触发告警</li>
 *   <li>{@link ErrorCodeException} 的 {@link com.kjs.wuli3.core.error.model.ErrorSeverity} 为
 *       CRITICAL 或 FATAL 时，即使是 4xx 也会告警（业务高危错误）</li>
 * </ul>
 *
 * <h2>传播错误处理</h2>
 * <p>对于跨服务传播的 {@link com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier}：
 * <ul>
 *   <li>ErrorOrigin 保留提供方的值，确保 HTTP 状态码反映原始责任归属</li>
 *   <li>ErrorSeverity 保留提供方的值，确保告警策略一致</li>
 *   <li>消息已经过提供方可见性过滤，消费方使用 PUBLIC 可见性输出</li>
 * </ul>
 *
 * @author GuoYang create on 2026/8/17 11:53
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
             * NativeResponseMode.ALL 表示调用方不需要 ApiResponse 外壳；框架异常仍以 ProblemDetail
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
        if (!WebExceptionHandler.shouldAlert(error, status)) {
            return;
        }
        this.errorAlertNotifiers.dispatch(new ErrorAlertContext(
                error,
                status,
                responseCode,
                this.requestId(),
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()));
    }

    /**
     * 判断错误是否需要发送告警通知。
     *
     * <p>告警触发条件：
     * <ul>
     *   <li>HTTP 5xx 状态码（服务端错误）</li>
     *   <li>或 {@link ErrorCodeException} 的严重程度为 {@link ErrorSeverity#CRITICAL} 或 {@link ErrorSeverity#FATAL}
     *       （即使是 4xx 调用方错误，高严重度也需要告警）</li>
     * </ul>
     *
     * <p>严重程度通过 {@link ErrorCodeException#getSeverity()} 获取，该方法会：
     * <ul>
     *   <li>对于本地错误枚举：从 {@code @ErrorMetadata} 注解中解析</li>
     *   <li>对于远程传播错误：使用传播协议中携带的 severity 值</li>
     * </ul>
     *
     * @param error 发生的异常
     * @param status 解析后的 HTTP 状态码
     * @return true 表示需要发送告警
     */
    private static boolean shouldAlert(final Throwable error, final HttpStatus status) {
        if (status.is5xxServerError()) {
            return true;
        }
        if (error instanceof ErrorCodeException errorCodeException) {
            final ErrorSeverity severity = errorCodeException.getSeverity();
            return severity == ErrorSeverity.CRITICAL || severity == ErrorSeverity.FATAL;
        }
        return false;
    }

    private @Nullable String requestId() {
        return this.invocationContextAccessor.requestId().orElse(null);
    }
}

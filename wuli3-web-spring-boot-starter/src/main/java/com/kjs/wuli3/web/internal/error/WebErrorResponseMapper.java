package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorVisibility;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.internal.advice.ApiResponseFactory;
import com.kjs.wuli3.web.response.ApiResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Web 错误处理过程共享的响应映射规则。
 *
 * <p><strong>核心职责：HTTP 边界安全过滤</strong></p>
 * <p>本类是 HTTP 边界错误可见性过滤的单一真实来源（Single Source of Truth），确保敏感错误信息不会泄露给外部调用方。
 * 所有错误响应（包括 {@link com.kjs.wuli3.web.response.ApiResponse} 和 {@link org.springframework.http.ProblemDetail}）
 * 都必须通过 {@link #visibleErrorCode} 和 {@link #visibleMessage} 过滤后才能输出。
 *
 * <h2>可见性过滤规则</h2>
 * <p>{@link #visibleErrorCode} 和 {@link #visibleMessage} 方法根据 {@link ErrorVisibility} 元数据实施以下过滤：
 * <ul>
 *   <li>{@link ErrorVisibility#PUBLIC}: 错误码和消息都对外输出（默认行为）</li>
 *   <li>{@link ErrorVisibility#CODE_ONLY}: 只输出错误码，消息替换为 {@code INTERNAL_ERROR} 的通用消息</li>
 *   <li>{@link ErrorVisibility#MESSAGE_ONLY}: 只输出消息，错误码替换为 {@code INTERNAL_ERROR}</li>
 *   <li>{@link ErrorVisibility#INTERNAL}: 错误码和消息都替换为 {@code INTERNAL_ERROR}，完全隐藏内部细节</li>
 * </ul>
 *
 * <p><strong>可见性优先级</strong>：运行时覆盖（{@link ErrorCodeException#withVisibility}）
 * &gt; 字段级 {@code @ErrorMetadata} &gt; 类级 {@code @ErrorMetadata} &gt; 默认值（PUBLIC）
 *
 * <h2>传播错误处理</h2>
 * <p>对于跨服务传播的 {@link com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier}：
 * <ul>
 *   <li>消息已经过提供方可见性过滤，消费方始终使用 {@link ErrorVisibility#PUBLIC} 输出</li>
 *   <li>{@link com.kjs.wuli3.core.error.model.ErrorOrigin} 和
 *       {@link com.kjs.wuli3.core.error.model.ErrorSeverity} 保留提供方的值，确保责任归属一致性</li>
 * </ul>
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class WebErrorResponseMapper {

    private static final String REQUEST_ID = "requestId";
    private static final String CODE = "code";

    private WebErrorResponseMapper() {}

    /**
     * 根据可见性策略返回允许对外输出的错误码。
     *
     * <p>MESSAGE_ONLY 和 INTERNAL 可见性会隐藏真实错误码，返回通用的 INTERNAL_ERROR。
     *
     * @param ex 包含错误码和可见性元数据的异常
     * @return 过滤后允许输出的错误码
     */
    public static ErrorCode visibleErrorCode(final ErrorCodeException ex) {
        final ErrorVisibility visibility = ex.getVisibility();
        if (visibility == ErrorVisibility.MESSAGE_ONLY || visibility == ErrorVisibility.INTERNAL) {
            return WebErrors.INTERNAL_ERROR;
        }
        return ex.getErrorCode();
    }

    /**
     * 根据可见性策略返回允许对外输出的错误消息。
     *
     * <p>CODE_ONLY 和 INTERNAL 可见性会隐藏真实消息，返回通用的 INTERNAL_ERROR 消息。
     *
     * @param ex 包含错误消息和可见性元数据的异常
     * @return 过滤后允许输出的错误消息
     */
    public static String visibleMessage(final ErrorCodeException ex) {
        final ErrorVisibility visibility = ex.getVisibility();
        if (visibility == ErrorVisibility.CODE_ONLY || visibility == ErrorVisibility.INTERNAL) {
            return WebErrors.INTERNAL_ERROR.getMessage();
        }
        final String message = ex.getMessage();
        return message == null ? ex.getErrorCode().getMessage() : message;
    }

    public static ErrorCode responseCode(final HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return WebErrors.UNAUTHORIZED;
        }
        if (status == HttpStatus.FORBIDDEN) {
            return WebErrors.FORBIDDEN;
        }
        if (status == HttpStatus.NOT_FOUND) {
            return WebErrors.NOT_FOUND;
        }
        if (status == HttpStatus.PAYLOAD_TOO_LARGE) {
            return WebErrors.PAYLOAD_TOO_LARGE;
        }
        if (status.is5xxServerError()) {
            return WebErrors.INTERNAL_ERROR;
        }
        return WebErrors.BAD_REQUEST;
    }

    public static String message(final Throwable ex) {
        final String message = ex.getMessage();
        return message == null ? WebErrors.INTERNAL_ERROR.getMessage() : message;
    }

    public static ResponseEntity<ProblemDetail> nativeError(
            final HttpStatus status,
            final ErrorCode responseCode,
            final String message,
            final @Nullable String requestId,
            final ApiResponseFactory responseFactory) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(message);
        problemDetail.setProperty(
                CODE, responseFactory.fail(responseCode, message).code());
        problemDetail.setProperty(REQUEST_ID, requestId);
        return ResponseEntity.status(status).body(problemDetail);
    }

    /** 使用已经执行可见性处理的响应数据创建原生 ProblemDetail。 */
    public static ResponseEntity<ProblemDetail> nativeError(final HttpStatus status, final ApiResponse<?> response) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(response.message());
        problemDetail.setProperty(CODE, response.code());
        problemDetail.setProperty(REQUEST_ID, response.requestId());
        return ResponseEntity.status(status).body(problemDetail);
    }

    public static ResponseEntity<ProblemDetail> nativeProblemDetail(
            final int statusCode, final String detail, final @Nullable String requestId) {
        final HttpStatus status = HttpStatus.valueOf(statusCode);
        final ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(detail);
        problemDetail.setProperty(REQUEST_ID, requestId);
        return ResponseEntity.status(status).body(problemDetail);
    }
}

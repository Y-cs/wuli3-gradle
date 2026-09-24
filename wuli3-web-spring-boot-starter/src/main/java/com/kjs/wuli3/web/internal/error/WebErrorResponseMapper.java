package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.response.ApiResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Web 错误处理过程共享的响应映射规则。
 *
 * <p>core 统一完成可见性过滤；Web 只投影展示字段，不输出传播模型的诊断信息。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class WebErrorResponseMapper {

    private static final String REQUEST_ID = "requestId";
    private static final String CODE = "code";

    private WebErrorResponseMapper() {}

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

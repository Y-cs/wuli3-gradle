package com.kjs.wuli3.web.internal.response;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.web.error.WebErrors;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Shared response mapping rules for handled web errors.
 */
final class WebErrorResponseMapper {

    private static final String REQUEST_ID = "requestId";
    private static final String CODE = "code";

    private WebErrorResponseMapper() {}

    static ErrorCode visibleErrorCode(final ErrorCodeException ex) {
        final ErrorVisibility visibility = ex.getResolvedErrorPolicy().visibility();
        if (visibility == ErrorVisibility.MESSAGE_ONLY || visibility == ErrorVisibility.INTERNAL) {
            return WebErrors.INTERNAL_ERROR;
        }
        return ex.getErrorCode();
    }

    static String visibleMessage(final ErrorCodeException ex) {
        final ErrorVisibility visibility = ex.getResolvedErrorPolicy().visibility();
        if (visibility == ErrorVisibility.CODE_ONLY || visibility == ErrorVisibility.INTERNAL) {
            return WebErrors.INTERNAL_ERROR.getMessage();
        }
        final String message = ex.getMessage();
        return message == null ? ex.getErrorCode().getMessage() : message;
    }

    static ErrorCode responseCode(final HttpStatus status) {
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

    static String message(final Throwable ex) {
        final String message = ex.getMessage();
        return message == null ? WebErrors.INTERNAL_ERROR.getMessage() : message;
    }

    static ResponseEntity<ProblemDetail> nativeError(
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

    static ResponseEntity<ProblemDetail> nativeProblemDetail(
            final int statusCode, final String detail, final @Nullable String requestId) {
        final HttpStatus status = HttpStatus.valueOf(statusCode);
        final ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(detail);
        problemDetail.setProperty(REQUEST_ID, requestId);
        return ResponseEntity.status(status).body(problemDetail);
    }
}

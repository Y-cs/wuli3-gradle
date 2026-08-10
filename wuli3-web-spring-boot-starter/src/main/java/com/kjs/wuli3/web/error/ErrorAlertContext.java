package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/**
 * Immutable snapshot passed to error alert notifiers.
 */
public record ErrorAlertContext(
        Throwable error,
        HttpStatus status,
        ErrorCode responseCode,
        @Nullable String requestId,
        String method,
        String requestUri,
        String remoteAddr) {}

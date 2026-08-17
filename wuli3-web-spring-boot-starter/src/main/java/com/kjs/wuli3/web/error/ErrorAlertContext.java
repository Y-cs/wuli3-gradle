package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/**
 * 传递给错误告警通知器的不可变快照。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record ErrorAlertContext(
        Throwable error,
        HttpStatus status,
        ErrorCode responseCode,
        @Nullable String requestId,
        String method,
        String requestUri,
        String remoteAddr) {}

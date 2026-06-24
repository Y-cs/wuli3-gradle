package com.kjs.wuli3.core.error;

import lombok.Getter;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * ErrorCodeException
 *
 * @author GuoYang create on 2026/6/24 10:59
 */
public class ErrorCodeException extends RuntimeException {

    private final ErrorPolicy errorPolicy = new ErrorPolicy();

    @Getter
    private final ErrorCode errorCode;

    public ErrorCodeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCodeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCodeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCodeException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCodeException policy(Consumer<ErrorPolicy> errorPolicyConsumer) {
        Optional.ofNullable(errorPolicyConsumer)
                .ifPresent(consumer -> consumer.accept(errorPolicy));
        return this;
    }

    public ErrorCodeException visibility(ErrorVisibility visibility) {
        if (visibility != null) {errorPolicy.visibility(visibility);}
        return this;
    }

    public ErrorCodeException severity(ErrorSeverity severity) {
        if (severity != null) {errorPolicy.severity(severity);}
        return this;
    }

}

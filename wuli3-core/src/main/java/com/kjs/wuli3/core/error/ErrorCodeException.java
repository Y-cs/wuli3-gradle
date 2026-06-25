package com.kjs.wuli3.core.error;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.function.UnaryOperator;

/**
 * ErrorCodeException
 *
 * @author GuoYang create on 2026/6/24 10:59
 */
@Getter
public class ErrorCodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    private ErrorPolicy errorPolicy;

    private transient @Nullable Object detail;

    public ErrorCodeException(ErrorCode errorCode, String message, @Nullable Throwable cause) {
        super(message != null ? message : errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.errorPolicy = ErrorModuleHolder.instance()
                .getErrorPolicy(errorCode);
    }

    public ErrorCodeException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    public ErrorCodeException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, errorCode.getMessage(), cause);
    }

    public ErrorCodeException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public ErrorCodeException policy(UnaryOperator<ErrorPolicy> errorPolicyUpdater) {
        if (errorPolicyUpdater == null) {
            return this;
        }
        ErrorPolicy policy = errorPolicyUpdater.apply(errorPolicy);
        if (policy != null) {
            this.errorPolicy = policy;
        }
        return this;
    }

    public ErrorCodeException visibility(ErrorVisibility visibility) {
        if (visibility != null) {
            errorPolicy = errorPolicy.withVisibility(visibility);
        }
        return this;
    }

    public ErrorCodeException severity(ErrorSeverity severity) {
        if (severity != null) {
            errorPolicy = errorPolicy.withSeverity(severity);
        }
        return this;
    }

    public ErrorCodeException detail(Object detail) {
        this.detail = detail;
        return this;
    }
}

package com.kjs.wuli3.core.error;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

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

    private ResolvedErrorPolicy resolvedErrorPolicy;

    private transient @Nullable Object detail;

    public ErrorCodeException(ErrorCode errorCode, String message, @Nullable Throwable cause) {
        super(message != null ? message : errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.resolvedErrorPolicy = ErrorMetadataParser.instance()
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

    public ErrorCodeException policy(@Nullable ErrorPolicyUpdater errorPolicyUpdater) {
        if (errorPolicyUpdater == null) {
            return this;
        }
        ResolvedErrorPolicy policy = errorPolicyUpdater.apply(resolvedErrorPolicy);
        if (policy != null) {
            this.resolvedErrorPolicy = policy;
        }
        return this;
    }

    public ErrorCodeException visibility(ErrorVisibility visibility) {
        if (visibility != null) {
            resolvedErrorPolicy = resolvedErrorPolicy.withVisibility(visibility);
        }
        return this;
    }

    public ErrorCodeException severity(ErrorSeverity severity) {
        if (severity != null) {
            resolvedErrorPolicy = resolvedErrorPolicy.withSeverity(severity);
        }
        return this;
    }

    public ErrorCodeException detail(Object detail) {
        this.detail = detail;
        return this;
    }

    @FunctionalInterface
    public interface ErrorPolicyUpdater {
        @Nullable ResolvedErrorPolicy apply(ResolvedErrorPolicy policy);
    }
}

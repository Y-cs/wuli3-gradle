package com.kjs.wuli3.core.error;

import java.io.Serial;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Runtime exception carrying a domain {@link ErrorCode} plus the resolved response policy.
 */
@Getter
public class ErrorCodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    private ResolvedErrorPolicy resolvedErrorPolicy;

    private transient @Nullable Object detail;

    public ErrorCodeException(final ErrorCode errorCode, final String message, final @Nullable Throwable cause) {
        super(message != null ? message : errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.resolvedErrorPolicy = ErrorMetadataParser.instance().getErrorPolicy(errorCode);
    }

    public ErrorCodeException(final ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    public ErrorCodeException(final ErrorCode errorCode, final Throwable cause) {
        this(errorCode, errorCode.getMessage(), cause);
    }

    public ErrorCodeException(final ErrorCode errorCode, final String message) {
        this(errorCode, message, null);
    }

    public ErrorCodeException policy(final @Nullable ErrorPolicyUpdater errorPolicyUpdater) {
        if (errorPolicyUpdater == null) {
            return this;
        }
        final ResolvedErrorPolicy policy = errorPolicyUpdater.apply(this.resolvedErrorPolicy);
        if (policy != null) {
            this.resolvedErrorPolicy = policy;
        }
        return this;
    }

    public ErrorCodeException visibility(final ErrorVisibility visibility) {
        if (visibility != null) {
            this.resolvedErrorPolicy = this.resolvedErrorPolicy.withVisibility(visibility);
        }
        return this;
    }

    public ErrorCodeException severity(final ErrorSeverity severity) {
        if (severity != null) {
            this.resolvedErrorPolicy = this.resolvedErrorPolicy.withSeverity(severity);
        }
        return this;
    }

    public ErrorCodeException detail(final Object detail) {
        this.detail = detail;
        return this;
    }

    @FunctionalInterface
    public interface ErrorPolicyUpdater {
        @Nullable
        ResolvedErrorPolicy apply(final ResolvedErrorPolicy policy);
    }
}

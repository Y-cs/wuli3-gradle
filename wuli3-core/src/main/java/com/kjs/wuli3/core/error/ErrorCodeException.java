package com.kjs.wuli3.core.error;

import java.io.Serial;
import java.util.Objects;
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
        super(Objects.requireNonNull(message, "message"), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.resolvedErrorPolicy = ErrorMetadataParser.instance().getErrorPolicy(this.errorCode);
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

    public ErrorCodeException policy(final ErrorPolicyUpdater errorPolicyUpdater) {
        Objects.requireNonNull(errorPolicyUpdater, "errorPolicyUpdater");
        this.resolvedErrorPolicy =
                Objects.requireNonNull(errorPolicyUpdater.apply(this.resolvedErrorPolicy), "updated error policy");
        return this;
    }

    public ErrorCodeException visibility(final ErrorVisibility visibility) {
        Objects.requireNonNull(visibility, "visibility");
        this.resolvedErrorPolicy = this.resolvedErrorPolicy.withVisibility(visibility);
        return this;
    }

    public ErrorCodeException severity(final ErrorSeverity severity) {
        Objects.requireNonNull(severity, "severity");
        this.resolvedErrorPolicy = this.resolvedErrorPolicy.withSeverity(severity);
        return this;
    }

    public ErrorCodeException detail(final @Nullable Object detail) {
        this.detail = detail;
        return this;
    }

    @FunctionalInterface
    public interface ErrorPolicyUpdater {
        ResolvedErrorPolicy apply(final ResolvedErrorPolicy policy);
    }
}

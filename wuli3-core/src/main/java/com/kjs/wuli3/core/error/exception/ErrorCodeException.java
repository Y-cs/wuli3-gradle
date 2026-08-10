package com.kjs.wuli3.core.error.exception;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.metadata.ErrorMetadataParser;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import com.kjs.wuli3.core.error.policy.ResolvedErrorPolicy;
import java.io.Serial;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** 携带错误码和已解析错误策略的运行时异常。 */
@Getter
public class ErrorCodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    private ResolvedErrorPolicy resolvedErrorPolicy;

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

    public ErrorCodeException origin(final ErrorOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        this.resolvedErrorPolicy = this.resolvedErrorPolicy.withOrigin(origin);
        return this;
    }
}

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

/** 携带错误码和已解析错误策略的运行时异常。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
public class ErrorCodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    private ResolvedErrorPolicy resolvedErrorPolicy;

    /** 使用错误码、消息和底层原因创建异常。 */
    public ErrorCodeException(final ErrorCode errorCode, final String message, final @Nullable Throwable cause) {
        super(Objects.requireNonNull(message, "message"), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.resolvedErrorPolicy = ErrorMetadataParser.instance().getErrorPolicy(this.errorCode);
    }

    /** 使用错误码的默认消息创建异常。 */
    public ErrorCodeException(final ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    /** 使用错误码默认消息和底层原因创建异常。 */
    public ErrorCodeException(final ErrorCode errorCode, final Throwable cause) {
        this(errorCode, errorCode.getMessage(), cause);
    }

    /** 使用错误码和自定义消息创建异常。 */
    public ErrorCodeException(final ErrorCode errorCode, final String message) {
        this(errorCode, message, null);
    }

    /** 覆盖当前异常的错误可见性策略。 */
    public ErrorCodeException visibility(final ErrorVisibility visibility) {
        Objects.requireNonNull(visibility, "visibility");
        this.resolvedErrorPolicy = this.resolvedErrorPolicy.withVisibility(visibility);
        return this;
    }

    /** 覆盖当前异常的错误严重程度策略。 */
    public ErrorCodeException severity(final ErrorSeverity severity) {
        Objects.requireNonNull(severity, "severity");
        this.resolvedErrorPolicy = this.resolvedErrorPolicy.withSeverity(severity);
        return this;
    }

    /** 覆盖当前异常的错误来源策略。 */
    public ErrorCodeException origin(final ErrorOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        this.resolvedErrorPolicy = this.resolvedErrorPolicy.withOrigin(origin);
        return this;
    }
}

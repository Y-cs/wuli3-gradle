package com.kjs.wuli3.core.error;

import com.kjs.wuli3.core.error.resolver.ErrorMetadataResolver;
import java.io.Serial;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * 携带错误标识的唯一运行时异常。
 *
 * <p>支持本地枚举错误码和传播协议错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
public class ErrorCodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    private @Nullable ErrorVisibility outputVisibility;

    /**
     * 使用错误码、消息和底层原因创建异常。
     */
    public ErrorCodeException(final ErrorCode errorCode, final String message, final @Nullable Throwable cause) {
        super(Objects.requireNonNull(message, "message"), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
    }

    /**
     * 使用错误码的默认消息创建异常。
     */
    public ErrorCodeException(final ErrorCode errorCode) {
        this(Objects.requireNonNull(errorCode, "errorCode"), errorCode.getMessage(), null);
    }

    /**
     * 使用错误码默认消息和底层原因创建异常。
     */
    public ErrorCodeException(final ErrorCode errorCode, final Throwable cause) {
        this(Objects.requireNonNull(errorCode, "errorCode"), errorCode.getMessage(), cause);
    }

    /**
     * 使用错误码和自定义消息创建异常。
     */
    public ErrorCodeException(final ErrorCode errorCode, final String message) {
        this(errorCode, message, null);
    }

    /**
     * 运行时覆盖输出可见性（仅影响当前抛出点）。
     */
    public ErrorCodeException withVisibility(final ErrorVisibility visibility) {
        this.outputVisibility = Objects.requireNonNull(visibility, "visibility");
        return this;
    }

    /**
     * 获取运行时覆盖的输出可见性。
     */
    public Optional<ErrorVisibility> getOutputVisibility() {
        return Optional.ofNullable(this.outputVisibility);
    }

    /**
     * 获取错误责任归属。
     */
    public ErrorOrigin getOrigin() {
        return ErrorMetadataResolver.instance().getOrigin(this.errorCode);
    }

    /**
     * 获取错误严重程度。
     */
    public ErrorSeverity getSeverity() {
        return ErrorMetadataResolver.instance().getSeverity(this.errorCode);
    }

    /**
     * 获取错误输出可见性（优先使用运行时覆盖）。
     */
    public ErrorVisibility getVisibility() {
        return Objects.requireNonNullElseGet(
                this.outputVisibility, () -> ErrorMetadataResolver.instance().getVisibility(this.errorCode));
    }

    /** 判断是否为远程传播错误。 */
    public boolean isRemoteError() {
        return this.errorCode instanceof ErrorPropagationProtocol;
    }

    /** 获取传播协议数据（如果是远程错误）。 */
    public Optional<ErrorPropagationProtocol> asRemoteError() {
        if (this.errorCode instanceof ErrorPropagationProtocol protocol) {
            return Optional.of(protocol);
        }
        return Optional.empty();
    }
}

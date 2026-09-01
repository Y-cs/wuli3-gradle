package com.kjs.wuli3.core.error.propagation;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import java.util.Objects;

/**
 * 跨服务边界传输的错误值，同时也是接收方可携带的 {@link ErrorCode}。
 *
 * @param code 完整稳定错误码
 * @param message 按边界可见性处理后的消息
 * @param origin 错误责任归属
 * @param severity 错误严重程度
 * @param sourceService 错误来源服务
 * @author GuoYang create on 2026/8/28 20:00
 */
public record ErrorCodeCarrier(
        String code, String message, ErrorOrigin origin, ErrorSeverity severity, String sourceService)
        implements ErrorCode {

    /** 校验传输值字段完整且错误码非空。 */
    public ErrorCodeCarrier {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(sourceService, "sourceService");
        if (code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
    }

    /** 返回按边界处理后的错误消息。 */
    @Override
    public String getMessage() {
        return this.message;
    }

    /** 从完整错误码提取末段错误名称。 */
    @Override
    public String getName() {
        final int lastDot = this.code.lastIndexOf('.');
        return lastDot < 0 ? this.code : this.code.substring(lastDot + 1);
    }

    /** 返回传播协议类型，避免接收方尝试加载提供方业务枚举。 */
    @Override
    public Class<? extends ErrorCode> getErrorType() {
        return ErrorCodeCarrier.class;
    }

    /** 判断错误码是否为跨服务接收的错误。 */
    public static boolean isRemoteError(final ErrorCode errorCode) {
        return errorCode instanceof ErrorCodeCarrier;
    }
}

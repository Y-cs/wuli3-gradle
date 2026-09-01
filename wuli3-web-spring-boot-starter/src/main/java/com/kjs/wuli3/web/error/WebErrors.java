package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorMetadata;
import com.kjs.wuli3.core.error.model.ErrorModule;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Web starter 定义的错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(
        name = "WEB",
        defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.CALLER, severity = ErrorSeverity.NORMAL))
public enum WebErrors implements ErrorCode {
    SUCCESS("成功"),
    BAD_REQUEST("请求参数错误"),

    @ErrorMetadata(severity = ErrorSeverity.WARNING)
    UNAUTHORIZED("未认证"),

    @ErrorMetadata(severity = ErrorSeverity.WARNING)
    FORBIDDEN("无权限"),

    NOT_FOUND("资源不存在"),
    PAYLOAD_TOO_LARGE("请求体过大"),

    @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.WARNING)
    INTERNAL_ERROR("内部错误"),

    @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.WARNING)
    SERVICE_CODE_NOT_FOUND("服务编码未设置");

    private final String message;
}

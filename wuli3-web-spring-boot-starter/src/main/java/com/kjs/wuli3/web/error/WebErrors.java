package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.metadata.ErrorModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Web starter 定义的错误码。
 */
@Getter
@RequiredArgsConstructor
@ErrorModule("WEB")
public enum WebErrors implements ErrorCode {
    SUCCESS("成功"),
    BAD_REQUEST("请求参数错误"),
    UNAUTHORIZED("未认证"),
    FORBIDDEN("无权限"),
    NOT_FOUND("资源不存在"),
    PAYLOAD_TOO_LARGE("请求体过大"),
    INTERNAL_ERROR("内部错误"),
    SERVICE_CODE_NOT_FOUND("服务编码未设置");

    private final String message;
}

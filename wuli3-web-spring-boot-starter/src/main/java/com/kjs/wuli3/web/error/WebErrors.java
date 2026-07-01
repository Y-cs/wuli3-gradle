package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorModule;
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
    INTERNAL_ERROR("内部错误"),
    SERVICE_CODE_NOT_FOUND("服务编码未设置");

    private final String message;
}

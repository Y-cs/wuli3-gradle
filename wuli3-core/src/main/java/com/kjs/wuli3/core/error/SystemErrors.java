package com.kjs.wuli3.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SystemErrorCode
 *
 * @author GuoYang create on 2026/6/24 10:15
 */
@Getter
@RequiredArgsConstructor
@ErrorModule("SYSTEM")
public enum SystemErrors implements ErrorCode {
    INTERNAL_ERROR("内部错误"),
    UNSUPPORTED_OPERATION("不支持的操作"),
    NOT_IMPLEMENTED("未实现"),
    ILLEGAL_STATE("非法状态异常"),
    ILLEGAL_ARGUMENT("非法参数异常"),
    ;

    private final String message;
}

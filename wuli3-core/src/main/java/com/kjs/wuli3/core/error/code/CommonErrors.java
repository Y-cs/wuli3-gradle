package com.kjs.wuli3.core.error.code;

import com.kjs.wuli3.core.error.metadata.ErrorModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 跨模块可复用的通用调用方错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule("COMMON")
public enum CommonErrors implements ErrorCode {
    UNSUPPORTED_OPERATION("不支持的操作"),

    ILLEGAL_STATE("非法状态异常"),

    ILLEGAL_ARGUMENT("非法参数异常");

    private final String message;
}

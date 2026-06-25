package com.kjs.wuli3.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ErrorFrameworkErrors
 *
 * @author GuoYang create on 2026/6/24 14:39
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(value = "ERROR_FRAMEWORK", visibility = ErrorVisibility.INTERNAL)
public enum ErrorFrameworkErrors implements ErrorCode {

    ERROR_CODE_RESOLVE_FAILED("错误代码解释失败"),

    INVALID_ERROR_CODE("无效错误代码"),

    MODULE_NOT_FOUND("未找到错误模型");

    private final String message;
}


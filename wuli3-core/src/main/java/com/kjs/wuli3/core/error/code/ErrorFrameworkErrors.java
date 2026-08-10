package com.kjs.wuli3.core.error.code;

import com.kjs.wuli3.core.error.metadata.ErrorModule;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 错误模型自身使用的内部错误码。 */
@Getter
@RequiredArgsConstructor
@ErrorModule(
        value = "ERROR_FRAMEWORK",
        policy =
                @ErrorPolicy(
                        severity = ErrorSeverity.CRITICAL,
                        visibility = ErrorVisibility.INTERNAL,
                        origin = ErrorOrigin.SYSTEM))
public enum ErrorFrameworkErrors implements ErrorCode {
    ERROR_CODE_RESOLVE_FAILED("错误代码解释失败"),

    INVALID_ERROR_CODE("无效错误代码"),

    INVALID_ERROR_MODULE("无效错误模块"),

    MODULE_NOT_FOUND("未找到错误模型");

    private final String message;
}

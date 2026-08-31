package com.kjs.wuli3.core.error.builtin;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorMetadata;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.core.error.ErrorOrigin;
import com.kjs.wuli3.core.error.ErrorSeverity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 错误模型自身使用的内部错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(
        name = "ERROR_FRAMEWORK",
        defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.CRITICAL))
public enum ErrorFrameworkErrors implements ErrorCode {
    ERROR_CODE_RESOLVE_FAILED("错误代码解释失败"),

    INVALID_ERROR_CODE("无效错误代码"),

    INVALID_ERROR_MODULE("无效错误模块"),

    MODULE_NOT_FOUND("未找到错误模型");

    private final String message;
}

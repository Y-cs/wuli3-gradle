package com.kjs.wuli3.core.error.builtin;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorMetadata;
import com.kjs.wuli3.core.error.model.ErrorModule;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 跨模块可复用的系统级错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(
        name = "SYSTEM",
        defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.CRITICAL))
public enum SystemErrors implements ErrorCode {
    INTERNAL_ERROR("内部错误"),

    CONFIGURATION_MISSING("运行配置缺失"),

    NOT_IMPLEMENTED("未实现");

    private final String message;
}

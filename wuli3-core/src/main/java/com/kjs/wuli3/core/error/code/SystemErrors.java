package com.kjs.wuli3.core.error.code;

import com.kjs.wuli3.core.error.metadata.ErrorModule;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 跨模块可复用的系统级错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(
        value = "SYSTEM",
        policy =
                @ErrorPolicy(
                        severity = ErrorSeverity.CRITICAL,
                        visibility = ErrorVisibility.INTERNAL,
                        origin = ErrorOrigin.SYSTEM))
public enum SystemErrors implements ErrorCode {
    INTERNAL_ERROR("内部错误"),

    CONFIGURATION_MISSING("运行配置缺失"),

    NOT_IMPLEMENTED("未实现");

    private final String message;
}

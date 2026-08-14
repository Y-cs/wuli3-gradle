package com.kjs.wuli3.aliyun.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.metadata.ErrorModule;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OssErrorCode
 * @author GuoYang create on 2026/8/12 17:38
 */
@ErrorModule(
        value = "OSS",
        policy =
                @ErrorPolicy(
                        severity = ErrorSeverity.WARNING,
                        visibility = ErrorVisibility.PUBLIC,
                        origin = ErrorOrigin.CALLER))
@RequiredArgsConstructor
public enum OssErrorCode implements ErrorCode {
    @ErrorPolicy(severity = ErrorSeverity.CRITICAL, visibility = ErrorVisibility.INTERNAL, origin = ErrorOrigin.SYSTEM)
    OPERATE_ERROR("OSS 操作失败"),
    PROFILE_NOT_FOUND("OSS 配置不存在"),
    DEFAULT_PROFILE_MISSING("OSS 默认配置未设置"),
    DEFAULT_PROFILE_INVALID("OSS 默认配置无效"),
    INVALID_PROFILE("OSS 配置无效"),
    @ErrorPolicy(severity = ErrorSeverity.CRITICAL, visibility = ErrorVisibility.INTERNAL, origin = ErrorOrigin.SYSTEM)
    CLIENT_CLOSE_FAILED("OSS 客户端关闭失败");

    @Getter
    private final String message;
}

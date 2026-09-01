package com.kjs.wuli3.aliyun.error;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorMetadata;
import com.kjs.wuli3.core.error.model.ErrorModule;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OSS 模块错误码。
 *
 * @author GuoYang create on 2026/8/12 17:38
 */
@ErrorModule(name = "OSS", defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.CALLER, severity = ErrorSeverity.WARNING))
@RequiredArgsConstructor
public enum OssErrorCode implements ErrorCode {
    @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.CRITICAL)
    OPERATE_ERROR("OSS 操作失败"),

    PROFILE_NOT_FOUND("OSS 配置不存在"),
    DEFAULT_PROFILE_MISSING("OSS 默认配置未设置"),
    DEFAULT_PROFILE_INVALID("OSS 默认配置无效"),
    INVALID_PROFILE("OSS 配置无效"),

    @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.CRITICAL)
    CLIENT_CLOSE_FAILED("OSS 客户端关闭失败");

    @Getter
    private final String message;
}

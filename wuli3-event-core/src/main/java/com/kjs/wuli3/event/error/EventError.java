package com.kjs.wuli3.event.error;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorMetadata;
import com.kjs.wuli3.core.error.model.ErrorModule;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 事件模块错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@RequiredArgsConstructor
@Getter
@ErrorModule(name = "EVENT", defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.CALLER, severity = ErrorSeverity.NORMAL))
public enum EventError implements ErrorCode {
    /** 传输层不支持请求的事件发布能力。 */
    UNSUPPORTED_CAPABILITY("Unsupported Capability"),

    /** 事件发送失败。 */
    @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.CRITICAL)
    SEND_FAILED("Send Failed");

    private final String message;
}

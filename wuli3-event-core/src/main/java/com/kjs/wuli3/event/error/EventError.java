package com.kjs.wuli3.event.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.metadata.ErrorModule;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 事件模块错误码。 */
@RequiredArgsConstructor
@Getter
@ErrorModule("EVENT")
public enum EventError implements ErrorCode {
    /** 传输层不支持请求的事件发布能力。 */
    UNSUPPORTED_CAPABILITY("Unsupported Capability"),
    /** 事件发送失败。 */
    @ErrorPolicy(severity = ErrorSeverity.CRITICAL, visibility = ErrorVisibility.INTERNAL, origin = ErrorOrigin.SYSTEM)
    SEND_FAILED("Send Failed"),
    ;
    private final String message;
}

package com.kjs.wuli3.event.error;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorModule;
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
    SEND_FAILED("Send Failed"),
    ;
    private final String message;
}

package com.kjs.wuli3.event.error;

import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import java.io.Serial;

/**
 * UnsupportedCapabilityException 当传输实现无法满足请求的发布能力时抛出
 * @author GuoYang create on 2026/8/6 10:30
 */
public class UnsupportedCapabilityException extends ErrorCodeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 使用默认能力说明创建异常。 */
    public UnsupportedCapabilityException() {
        super(EventError.UNSUPPORTED_CAPABILITY, "Unsupported capability");
    }

    /**
     * 创建描述不受支持能力的异常。
     *
     * @param message 能力说明
     */
    public UnsupportedCapabilityException(final String message) {
        super(EventError.UNSUPPORTED_CAPABILITY, message);
    }
}

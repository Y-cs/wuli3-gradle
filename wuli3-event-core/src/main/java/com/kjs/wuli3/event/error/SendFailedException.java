package com.kjs.wuli3.event.error;

import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import java.io.Serial;

/**
 * SendFailedException 当传输层发送事件失败时抛出
 * @author GuoYang create on 2026/8/6 10:31
 */
public class SendFailedException extends ErrorCodeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建描述发送失败原因的异常。
     *
     * @param message 失败说明
     * @param cause 底层原因
     */
    public SendFailedException(final String message, final Throwable cause) {
        super(EventError.SEND_FAILED, message, cause);
    }
}

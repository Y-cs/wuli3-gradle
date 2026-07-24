package com.kjs.wuli3.event;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.event.error.EventError;
import java.io.Serial;
import java.util.Collection;

/** 通过具体的本地或远程投递机制发送事件信封。 */
public interface EventMessageTransport {

    /**
     * 按请求的传输能力发送一个事件。
     *
     * @param envelope 待发送事件
     * @param options 请求的通道和投递能力
     */
    void send(final EventEnvelope<?> envelope, final PublishOptions options);

    /**
     * 按请求的传输能力发送多个事件。
     *
     * @param envelopes 待发送事件集合
     * @param options 请求的通道和投递能力
     */
    void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options);

    /** 当传输实现无法满足请求的发布能力时抛出。 */
    @ErrorModule("EVENT")
    final class UnsupportedCapabilityException extends ErrorCodeException {
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

    /** 当传输层发送事件失败时抛出。 */
    @ErrorModule("EVENT")
    final class SendFailedException extends ErrorCodeException {
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
}

package com.kjs.wuli3.rocket.internal.wrapper;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * 在不依赖任一 RocketMQ Producer SDK 的前提下编码公共事件线协议。
 */
public final class RocketMessageWrapperEncoder {

    private static final int MAX_TOPIC_BYTES = 127;
    private static final Pattern TOPIC_PATTERN = Pattern.compile("[%a-zA-Z0-9_-]+");
    private final @Nullable ContextReader contextReader;
    private final ContextEncoder contextEncoder;

    /**
     * 使用当前上下文重建保留传播头信息的编码器。
     *
     * @param contextReader  可选的当前上下文读取器
     * @param contextEncoder 上下文字段编码器
     */
    public RocketMessageWrapperEncoder(
            final @Nullable ContextReader contextReader, final ContextEncoder contextEncoder) {
        this.contextReader = contextReader;
        this.contextEncoder = Objects.requireNonNull(contextEncoder, "contextEncoder");
    }

    /**
     * 校验并序列化事件，不调用任一 RocketMQ 客户端 SDK。
     *
     * @param envelope 待序列化事件
     * @param options  请求的远程投递能力
     * @return 与 SDK 无关的线消息
     */
    public RocketMessageWrapper encode(final EventEnvelope<?> envelope, final PublishOptions options) {
        Objects.requireNonNull(envelope, "envelope");
        Objects.requireNonNull(options, "options");
        if (!options.isRemote()) {
            throw new ErrorCodeException(
                    SystemErrors.ILLEGAL_ARGUMENT, "RocketMQ transport requires the REMOTE channel");
        }
        RocketMessageWrapperEncoder.validateTopic(envelope.topic(), envelope.eventId());
        RocketMessageWrapperEncoder.validateCapabilities(options);

        final Map<String, Object> headers = this.propagationHeaders();
        return new RocketMessageWrapper(
                envelope.topic(),
                Jsons.toJsonBytes(envelope),
                headers,
                envelope.eventId(),
                envelope.eventType(),
                options.getOrderKey(),
                options.getDelayTime());
    }

    /**
     * 处理传播头
     *
     * @return 当前调用需要传播的消息头
     */
    private Map<String, Object> propagationHeaders() {
        final Map<String, Object> headers = new LinkedHashMap<>();
        if (this.contextReader == null) {
            return headers;
        }
        // 获取当前上下文并添加传播头
        final ContextSnapshot snapshot = this.contextReader.capture();
        this.contextEncoder.writeTo(snapshot, headers::put);
        return headers;
    }

    private static void validateCapabilities(final PublishOptions options) {
        final Duration delay = options.getDelayTime();
        if (delay == null) {
            return;
        }
        if (delay.isZero() || delay.isNegative()) {
            throw new ErrorCodeException(SystemErrors.ILLEGAL_ARGUMENT, "RocketMQ delay must be positive");
        }
        if (options.isAsync()) {
            throw new ErrorCodeException(
                    SystemErrors.UNSUPPORTED_OPERATION, "RocketMQ exact delay does not support async " + "publication");
        }
        if (options.getOrderKey() != null) {
            throw new ErrorCodeException(
                    SystemErrors.UNSUPPORTED_OPERATION,
                    "RocketMQ exact delay does not support " + "ordered publication");
        }
    }

    private static void validateTopic(final String topic, final String eventId) {
        Objects.requireNonNull(topic, "topic");
        if (!RocketMessageWrapperEncoder.TOPIC_PATTERN.matcher(topic).matches()
                || topic.getBytes(StandardCharsets.UTF_8).length > RocketMessageWrapperEncoder.MAX_TOPIC_BYTES) {
            throw new ErrorCodeException(
                    SystemErrors.ILLEGAL_ARGUMENT,
                    "Invalid RocketMQ topic for event " + eventId
                            + "; use 1-127 bytes containing only letters, digits, %, -, or _");
        }
    }
}

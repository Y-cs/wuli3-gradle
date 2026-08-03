package com.kjs.wuli3.rocketmq.internal;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventMessageTransport.UnsupportedCapabilityException;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.encoding.AuthContextEncoder;
import com.kjs.wuli3.propagation.encoding.InvocationContextEncoder;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.rocketmq.autoconfigure.RocketMqContextMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * 在不依赖任一 RocketMQ Producer SDK 的前提下编码公共事件线协议。
 */
public final class RocketMqEventMessageEncoder {

    private static final int MAX_TOPIC_BYTES = 127;
    private static final Pattern TOPIC_PATTERN = Pattern.compile("[%a-zA-Z0-9_-]+");
    private static final Set<String> RESERVED_HEADERS = Set.of(
            InvocationContextEncoder.REQUEST_ID.toLowerCase(Locale.ROOT),
            InvocationContextEncoder.ORIGIN_IP.toLowerCase(Locale.ROOT),
            AuthContextEncoder.USER_ID.toLowerCase(Locale.ROOT),
            AuthContextEncoder.USERNAME.toLowerCase(Locale.ROOT));

    private final @Nullable ContextReader contextReader;
    private final RocketMqContextMode contextMode;

    /**
     * 使用当前上下文重建保留传播头信息的编码器。
     *
     * @param contextReader 可选的当前上下文读取器
     * @param contextMode   可跨越 RocketMQ 边界的上下文类型
     */
    public RocketMqEventMessageEncoder(
            final @Nullable ContextReader contextReader, final RocketMqContextMode contextMode) {
        this.contextReader = contextReader;
        this.contextMode = Objects.requireNonNull(contextMode, "contextMode");
    }

    /**
     * 校验并序列化事件，不调用任一 RocketMQ 客户端 SDK。
     *
     * @param envelope 待序列化事件
     * @param options  请求的远程投递能力
     * @return 与 SDK 无关的线消息
     */
    public RocketMqWireMessage encode(final EventEnvelope<?> envelope, final PublishOptions options) {
        Objects.requireNonNull(envelope, "envelope");
        Objects.requireNonNull(options, "options");
        if (!options.isRemote()) {
            throw new IllegalArgumentException("RocketMQ transport requires the REMOTE channel");
        }
        RocketMqEventMessageEncoder.validateTopic(envelope.topic(), envelope.eventId());
        RocketMqEventMessageEncoder.validateCapabilities(options);

        final Map<String, Object> headers = this.propagationSafeHeaders(envelope.headers());
        final EventEnvelope<?> wireEnvelope = new EventEnvelope<>(
                headers,
                envelope.topic(),
                envelope.eventType(),
                envelope.eventId(),
                envelope.occurredOn(),
                envelope.payload());
        return new RocketMqWireMessage(
                envelope.topic(),
                Jsons.toJsonBytes(wireEnvelope),
                envelope.eventId(),
                envelope.eventType(),
                options.getOrderKey(),
                options.getDelayTime());
    }

    private Map<String, Object> propagationSafeHeaders(final Map<String, Object> source) {
        final Map<String, Object> headers = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (!RocketMqEventMessageEncoder.RESERVED_HEADERS.contains(key.toLowerCase(Locale.ROOT))) {
                headers.put(key, value);
            }
        });
        final @Nullable ContextReader currentContextReader = this.contextReader;
        if (currentContextReader == null) {
            return headers;
        }
        final ContextSnapshot snapshot = currentContextReader.capture();
        snapshot.get(InvocationContext.class)
                .ifPresent(context -> InvocationContextEncoder.writeTo(context, headers::put));
        if (this.contextMode == RocketMqContextMode.TRUSTED_INTERNAL) {
            snapshot.get(AuthContext.class).ifPresent(context -> AuthContextEncoder.writeTo(context, headers::put));
        }
        return headers;
    }

    private static void validateCapabilities(final PublishOptions options) {
        final Duration delay = options.getDelayTime();
        if (delay == null) {
            return;
        }
        if (delay.isZero() || delay.isNegative()) {
            throw new IllegalArgumentException("RocketMQ delay must be positive");
        }
        if (options.isAsync()) {
            throw new UnsupportedCapabilityException("RocketMQ exact delay does not support async publication");
        }
        if (options.getOrderKey() != null) {
            throw new UnsupportedCapabilityException("RocketMQ exact delay does not support ordered publication");
        }
    }

    private static void validateTopic(final String topic, final String eventId) {
        Objects.requireNonNull(topic, "topic");
        if (!RocketMqEventMessageEncoder.TOPIC_PATTERN.matcher(topic).matches()
                || topic.getBytes(StandardCharsets.UTF_8).length > RocketMqEventMessageEncoder.MAX_TOPIC_BYTES) {
            throw new IllegalArgumentException("Invalid RocketMQ topic for event "
                    + eventId
                    + "; use 1-127 bytes containing only letters, digits, %, -, or _");
        }
    }
}

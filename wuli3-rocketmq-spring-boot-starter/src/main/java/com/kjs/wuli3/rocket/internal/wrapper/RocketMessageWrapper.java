package com.kjs.wuli3.rocket.internal.wrapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 由公共编码器生成、与 SDK 无关的 RocketMQ 事件消息。
 */
public record RocketMessageWrapper(
        String topic,
        byte[] body,
        Map<String, Object> headers,
        String key,
        String tag,
        @Nullable String orderKey,
        @Nullable Duration delay) {

    /**
     * 创建带有防御性拷贝的 SDK 无关消息值。
     *
     * @param topic    目标主题
     * @param body     序列化后的事件信封
     * @param headers  RocketMQ 消息属性
     * @param key      事件标识
     * @param tag      事件类型标签，用于 broker 侧过滤
     * @param orderKey 可选顺序键
     * @param delay    可选的精确延迟
     */
    public RocketMessageWrapper(
            final String topic,
            final byte[] body,
            final Map<String, Object> headers,
            final String key,
            final String tag,
            final @Nullable String orderKey,
            final @Nullable Duration delay) {
        this.topic = Objects.requireNonNull(topic, "topic");
        this.body = Arrays.copyOf(Objects.requireNonNull(body, "body"), body.length);
        this.headers = Map.copyOf(Objects.requireNonNull(headers, "headers"));
        this.key = Objects.requireNonNull(key, "key");
        this.tag = Objects.requireNonNull(tag, "tag");
        this.orderKey = orderKey;
        this.delay = delay;
    }
}

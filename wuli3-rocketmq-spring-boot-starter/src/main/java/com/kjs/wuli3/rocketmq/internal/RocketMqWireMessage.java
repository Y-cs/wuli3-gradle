package com.kjs.wuli3.rocketmq.internal;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

/**
 * 由公共编码器生成、与 SDK 无关的 RocketMQ 事件消息。
 */
public final class RocketMqWireMessage {

    private final String topic;
    private final byte[] body;
    private final String key;
    private final String tag;
    private final @Nullable String orderKey;
    private final @Nullable Duration delay;

    /**
     * 创建带有防御性拷贝的 SDK 无关消息值。
     *
     * @param topic    目标主题
     * @param body     序列化后的事件信封
     * @param key      事件标识
     * @param tag      事件类型标签，用于 broker 侧过滤
     * @param orderKey 可选顺序键
     * @param delay    可选的精确延迟
     */
    public RocketMqWireMessage(final String topic, final byte[] body, final String key, final String tag,
            final @Nullable String orderKey, final @Nullable Duration delay) {
        this.topic = Objects.requireNonNull(topic, "topic");
        this.body = Arrays.copyOf(Objects.requireNonNull(body, "body"), body.length);
        this.key = Objects.requireNonNull(key, "key");
        this.tag = Objects.requireNonNull(tag, "tag");
        this.orderKey = orderKey;
        this.delay = delay;
    }

    /**
     * 返回目标 RocketMQ 主题。
     *
     * @return 目标主题
     */
    public String topic() {
        return this.topic;
    }

    /**
     * 返回序列化事件信封的防御性副本。
     *
     * @return 序列化事件字节
     */
    public byte[] body() {
        return Arrays.copyOf(this.body, this.body.length);
    }

    /**
     * 返回用作 RocketMQ key 的事件标识。
     *
     * @return 事件 key
     */
    public String key() {
        return this.key;
    }

    /**
     * 返回用作 RocketMQ tag 的事件类型标签。
     *
     * @return 事件类型标签
     */
    public String tag() {
        return this.tag;
    }

    /**
     * 返回可选顺序键。
     *
     * @return 顺序键；未设置时为 null
     */
    public @Nullable String orderKey() {
        return this.orderKey;
    }

    /**
     * 返回可选的精确投递延迟。
     *
     * @return 精确延迟；未设置时为 null
     */
    public @Nullable Duration delay() {
        return this.delay;
    }
}

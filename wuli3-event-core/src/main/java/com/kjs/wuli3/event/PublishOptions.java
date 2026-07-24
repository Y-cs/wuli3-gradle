package com.kjs.wuli3.event;

import java.time.Duration;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * 不可变的发布通道与投递能力请求。
 */
@Getter
public final class PublishOptions {

    /**
     * 可用的发布通道。
     */
    public enum Channel {
        /**
         * 通过进程内 Spring 事件机制发布。
         */
        LOCAL,
        /**
         * 通过已配置的远程传输实现发布。
         */
        REMOTE
    }

    /**
     * 发布通道。
     */
    private final Channel channel;

    /**
     * 是否异步投递。
     */
    private final boolean async;

    /**
     * 是否等待事务提交后再投递。
     */
    private final boolean afterCommit;

    /**
     * 可选的投递延迟。
     */
    private final @Nullable Duration delayTime;

    /**
     * 可选的顺序键。
     */
    private final @Nullable String orderKey;

    /**
     * 为指定通道创建同步发布选项。
     *
     * @param channel 发布通道
     */
    public PublishOptions(final Channel channel) {
        this(channel, false, false, null, null);
    }

    private PublishOptions(
            final Channel channel,
            final boolean async,
            final boolean afterCommit,
            final @Nullable Duration delayTime,
            final @Nullable String orderKey) {
        this.channel = Objects.requireNonNull(channel, "channel cannot be null");
        this.async = async;
        this.afterCommit = afterCommit;
        this.delayTime = delayTime;
        this.orderKey = orderKey;
    }

    /**
     * 返回默认的同步本地发布选项。
     *
     * @return 默认本地选项
     */
    public static PublishOptions defaults() {
        return new PublishOptions(Channel.LOCAL);
    }

    /**
     * 判断所选通道是否为本地通道。
     *
     * @return 是否选择了 LOCAL
     */
    public boolean isLocal() {
        return this.channel == Channel.LOCAL;
    }

    /**
     * 判断所选通道是否为远程通道。
     *
     * @return 是否选择了 REMOTE
     */
    public boolean isRemote() {
        return this.channel == Channel.REMOTE;
    }

    /**
     * 返回设置了秒级延迟的副本。
     *
     * @param delaySeconds 请求的秒级延迟
     * @return 设置延迟后的选项副本
     */
    public PublishOptions delaySeconds(final long delaySeconds) {
        return this.setDelayTime(Duration.ofSeconds(delaySeconds));
    }

    /**
     * 返回设置了顺序键的副本。
     *
     * @param orderKey 非空白顺序键
     * @return 设置顺序键后的选项副本
     */
    public PublishOptions setOrderKey(final String orderKey) {
        final String requiredOrderKey = Objects.requireNonNull(orderKey, "orderKey cannot be null");
        if (requiredOrderKey.isBlank()) {
            throw new IllegalArgumentException("orderKey cannot be blank");
        }
        return new PublishOptions(this.channel, this.async, this.afterCommit, this.delayTime, requiredOrderKey);
    }

    /**
     * 返回设置了投递延迟的副本。
     *
     * @param delayTime 请求的延迟
     * @return 设置延迟后的选项副本
     */
    public PublishOptions setDelayTime(final Duration delayTime) {
        return new PublishOptions(
                this.channel,
                this.async,
                this.afterCommit,
                Objects.requireNonNull(delayTime, "delayTime cannot be null"),
                this.orderKey);
    }

    /**
     * 返回请求异步投递的副本。
     *
     * @return 异步选项副本
     */
    public PublishOptions async() {
        return new PublishOptions(this.channel, true, this.afterCommit, this.delayTime, this.orderKey);
    }

    /**
     * 返回请求事务提交后再投递的副本。
     *
     * @return afterCommit 选项副本
     */
    public PublishOptions afterCommit() {
        return new PublishOptions(this.channel, this.async, true, this.delayTime, this.orderKey);
    }
}

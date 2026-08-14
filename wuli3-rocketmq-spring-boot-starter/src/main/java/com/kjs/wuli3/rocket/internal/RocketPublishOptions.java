package com.kjs.wuli3.rocket.internal;

import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.options.AsyncPublishOptions;
import com.kjs.wuli3.event.options.TransactionalPublishOptions;
import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** RocketMQ 事件传输支持的不可变发布选项。 */
public record RocketPublishOptions(
        boolean async,
        boolean afterCommit,
        @Nullable Duration delay,
        @Nullable String orderKey) implements AsyncPublishOptions, TransactionalPublishOptions, PublishOptions {

    /** 创建同步、立即投递的默认选项。 */
    public RocketPublishOptions() {
        this(false, false, null, null);
    }

    /** 校验发布选项中的通用约束。 */
    public RocketPublishOptions {
        if (delay != null && (delay.isZero() || delay.isNegative())) {
            throw new IllegalArgumentException("delay must be positive");
        }
        if (orderKey != null && orderKey.isBlank()) {
            throw new IllegalArgumentException("orderKey cannot be blank");
        }
    }

    /** 返回启用异步发送的副本。 */
    public RocketPublishOptions withAsync() {
        return new RocketPublishOptions(true, this.afterCommit, this.delay, this.orderKey);
    }

    /** 返回要求事务提交后发送的副本。 */
    public RocketPublishOptions withAfterCommit() {
        return new RocketPublishOptions(this.async, true, this.delay, this.orderKey);
    }

    /** 返回设置了精确延迟的副本。 */
    public RocketPublishOptions withDelay(final Duration delay) {
        return new RocketPublishOptions(
                this.async, this.afterCommit, Objects.requireNonNull(delay, "delay cannot be null"), this.orderKey);
    }

    /** 返回设置了顺序键的副本。 */
    public RocketPublishOptions withOrderKey(final String orderKey) {
        return new RocketPublishOptions(
                this.async, this.afterCommit, this.delay, Objects.requireNonNull(orderKey, "orderKey cannot be null"));
    }
}

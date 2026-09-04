package com.kjs.wuli3.spring.shutdown;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * 向关闭钩子提供可见的时间预算和开始时间。
 *
 * <p>上下文对象在每个关闭阶段开始时创建，并传递给该阶段的所有钩子。
 * 钩子可以通过 {@link #remaining()} 方法查询剩余时间，以便在超时前完成工作。
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Override
 * public void shutdown(ShutdownContext context) throws InterruptedException {
 *     while (!queue.isEmpty() && !context.remaining().isZero()) {
 *         // 处理队列中的任务，但不超过剩余时间
 *         queue.poll(context.remaining().toMillis(), TimeUnit.MILLISECONDS);
 *     }
 * }
 * }</pre>
 *
 * @author GuoYang create on 2026/9/3 14:55
 * @see ShutdownHook#shutdown(ShutdownContext)
 */
public final class ShutdownContext {

    private final Instant startedAt;
    private final Duration timeout;

    /**
     * 创建关闭上下文。
     *
     * @param startedAt 关闭阶段的开始时间
     * @param timeout 允许的最大执行时长
     * @throws IllegalArgumentException 如果超时时长为负数
     */
    public ShutdownContext(final Instant startedAt, final Duration timeout) {
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must not be negative");
        }
    }

    /**
     * 返回当前关闭作用域的开始时间。
     *
     * @return 阶段开始的时间戳
     */
    public Instant getStartedAt() {
        return this.startedAt;
    }

    /**
     * 返回当前关闭作用域允许的最大时长。
     *
     * @return 配置的超时时长
     */
    public Duration getTimeout() {
        return this.timeout;
    }

    /**
     * 返回剩余时间，超过截止时间后固定返回零。
     *
     * <p>此方法计算从 {@link #getStartedAt()} 到当前时刻的已用时间，
     * 并返回 {@code timeout - elapsed}。如果已经超时，则返回 {@link Duration#ZERO}。
     *
     * <p>钩子应该定期调用此方法检查剩余时间，并在时间不足时提前退出。
     *
     * @return 剩余的执行时间，最小为零
     */
    public Duration remaining() {
        final Duration elapsed = Duration.between(this.startedAt, Instant.now());
        if (elapsed.compareTo(this.timeout) >= 0) {
            return Duration.ZERO;
        }
        return this.timeout.minus(elapsed);
    }
}

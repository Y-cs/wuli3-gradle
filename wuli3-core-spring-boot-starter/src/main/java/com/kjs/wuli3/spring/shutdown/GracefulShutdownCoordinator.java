package com.kjs.wuli3.spring.shutdown;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * 优雅关闭协调器，负责按顺序执行所有关闭阶段。
 *
 * <p>协调器是优雅关闭的顶层组件，集成到 Spring 容器的生命周期中。
 * 当容器关闭时，协调器会按照 {@link ShutdownPhase} 的声明顺序依次执行各个阶段。
 *
 * <h3>执行流程</h3>
 * <ol>
 *   <li>容器关闭时触发 {@link #stop()}</li>
 *   <li>关闭注册表，禁止新钩子注册</li>
 *   <li>依次执行所有 {@link ShutdownPhase}</li>
 *   <li>每个阶段在关闭线程中顺序执行</li>
 *   <li>无论成功失败，都继续下一阶段</li>
 * </ol>
 *
 * <h3>执行时机</h3>
 * <p>协调器的关闭阶段设置为 {@link #SHUTDOWN_PHASE}，位于 Spring Boot Web graceful
 * lifecycle 之后；Web Server、服务摘除和入口流量治理不属于本协调器的内部阶段。
 *
 * <h3>线程安全</h3>
 * <p>使用 {@link AtomicBoolean} 确保关闭流程只执行一次，
 * 即使在并发关闭场景下也不会重复执行。
 *
 * @author GuoYang create on 2026/9/4
 * @see ShutdownPhase
 * @see ShutdownHookRegistry
 */
public final class GracefulShutdownCoordinator implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownCoordinator.class);

    /**
     * 在 Spring Boot Web Server graceful lifecycle 之后执行。
     *
     * <p>Spring Boot 当前使用 {@code DEFAULT_PHASE - 1024}，因此较小的值会在其后停止。
     */
    public static final int SHUTDOWN_PHASE = SmartLifecycle.DEFAULT_PHASE - 2048;

    private final ShutdownHookRegistry registry;
    private final Function<ShutdownPhase, Duration> timeoutProvider;
    private final AtomicBoolean running = new AtomicBoolean();

    /**
     * 创建优雅关闭协调器。
     *
     * @param registry 钩子注册表
     * @param timeoutProvider 超时提供者，根据阶段返回超时时间
     */
    public GracefulShutdownCoordinator(
            final ShutdownHookRegistry registry, final Function<ShutdownPhase, Duration> timeoutProvider) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.timeoutProvider = Objects.requireNonNull(timeoutProvider, "timeoutProvider");
    }

    @Override
    public void start() {
        this.running.set(true);
    }

    /**
     * 执行优雅关闭编排。
     *
     * <p>使用 CAS 操作确保编排器只执行一次，即使在并发关闭场景下也是如此。
     */
    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            this.shutdown();
        }
    }

    /**
     * 执行优雅关闭编排，并在完成后调用回调。
     *
     * <p>无论编排成功还是失败，回调都会被执行，以确保 Spring 容器能够继续关闭流程。
     *
     * @param callback 关闭完成后要执行的回调
     */
    @Override
    public void stop(final Runnable callback) {
        try {
            this.stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    /**
     * 返回此生命周期组件的关闭阶段。
     *
     * <p>较小的阶段值会在关闭时较晚执行，因此此值位于 Spring Boot Web graceful lifecycle 之后。
     *
     * @return {@link #SHUTDOWN_PHASE}
     */
    @Override
    public int getPhase() {
        return GracefulShutdownCoordinator.SHUTDOWN_PHASE;
    }

    /**
     * 执行所有关闭阶段。
     *
     * <p>此方法会依次执行所有 {@link ShutdownPhase}，无论某个阶段成功还是失败，
     * 都会继续执行下一个阶段，确保关闭流程完整。
     */
    private void shutdown() {
        this.registry.closeRegistration();
        for (final ShutdownPhase phase : ShutdownPhase.values()) {
            this.executePhase(phase);
        }
    }

    /**
     * 执行单个关闭阶段。
     *
     * <p>为阶段创建时间上下文并顺序执行钩子。钩子负责使用上下文剩余时间
     * 限制自身的等待；单个钩子失败不会阻止后续钩子或阶段。
     *
     * @param phase 要执行的阶段
     */
    private void executePhase(final ShutdownPhase phase) {
        final Duration timeout = Objects.requireNonNull(this.timeoutProvider.apply(phase), "phase timeout");
        final ShutdownContext context = new ShutdownContext(Instant.now(), timeout);
        final List<ShutdownHook> hooks = this.registry.getHooks(phase);
        GracefulShutdownCoordinator.LOGGER.info(
                "Graceful shutdown phase started: phase={}, hooks={}, timeoutMs={}",
                phase,
                hooks.size(),
                timeout.toMillis());
        final long startedNanos = System.nanoTime();
        for (final ShutdownHook hook : hooks) {
            if (context.remaining().isZero()) {
                GracefulShutdownCoordinator.LOGGER.warn(
                        "Graceful shutdown phase budget exhausted: phase={}, hook={}", phase, hook.name());
                break;
            }
            final long hookStartedNanos = System.nanoTime();
            try {
                hook.shutdown(context);
                GracefulShutdownCoordinator.LOGGER.info(
                        "Graceful shutdown hook completed: phase={}, hook={}, elapsedMs={}",
                        phase,
                        hook.name(),
                        (System.nanoTime() - hookStartedNanos) / 1_000_000);
            } catch (final InterruptedException exception) {
                Thread.currentThread().interrupt();
                GracefulShutdownCoordinator.LOGGER.warn(
                        "Graceful shutdown hook interrupted: phase={}, hook={}", phase, hook.name(), exception);
                break;
            } catch (final RuntimeException exception) {
                GracefulShutdownCoordinator.LOGGER.warn(
                        "Graceful shutdown hook failed: phase={}, hook={}", phase, hook.name(), exception);
            }
        }
        GracefulShutdownCoordinator.LOGGER.info(
                "Graceful shutdown phase completed: phase={}, elapsedMs={}",
                phase,
                (System.nanoTime() - startedNanos) / 1_000_000);
    }
}

package com.kjs.wuli3.spring.autoconfigure;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.kjs.wuli3.spring.shutdown.ShutdownPhase;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

/**
 * 配置公共优雅关闭协调器。
 *
 * <p>此类绑定 {@code wuli3.spring.shutdown} 前缀下的配置属性，
 * 用于控制优雅关闭的行为和超时设置。
 *
 * <h3>配置示例</h3>
 * <pre>
 * # 全局配置
 * wuli3.spring.shutdown.enabled=true
 * wuli3.spring.shutdown.phase-timeout=30s
 *
 * # 特定阶段的超时配置
 * wuli3.spring.shutdown.phases.DRAIN_ASYNC_TASKS.timeout=60s
 * wuli3.spring.shutdown.phases.AWAIT_REMOTE_ACK.timeout=45s
 * </pre>
 *
 * <h3>超时配置策略</h3>
 * <p>每个关闭阶段都有时间预算，钩子应在预算内主动完成：
 * <ul>
 *   <li>使用 {@link #phaseTimeout} 作为所有阶段的默认超时</li>
 *   <li>可以通过 {@link #phases} 为特定阶段设置不同的超时</li>
 *   <li>建议根据实际业务场景调整超时时间，避免关闭时间过长</li>
 * </ul>
 *
 * @author GuoYang create on 2026/9/3 14:55
 * @see GracefulShutdownAutoConfiguration
 * @see ShutdownPhase
 */
@Getter
@ConfigurationProperties(prefix = "wuli3.spring.shutdown")
public class GracefulShutdownProperties {

    /**
     * 是否启用优雅关闭总线和生命周期协调器。
     *
     * <p>设置为 {@code false} 可以完全禁用优雅关闭功能。
     * 默认为 {@code true}。
     */
    @Setter
    private boolean enabled = true;

    /**
     * 每个关闭阶段允许的最大执行时长。
     *
     * <p>此值作为所有阶段的默认时间预算，可以通过 {@link #phases}
     * 为特定阶段设置不同的预算。钩子应主动使用上下文剩余时间返回，协调器不会强制中断阻塞调用。
     *
     * <p>默认为 30 秒。支持的时间单位包括：ms、s、m、h。
     */
    @DurationUnit(SECONDS)
    private Duration phaseTimeout = Duration.ofSeconds(30);

    /**
     * 各关闭阶段可选的超时覆盖配置。
     *
     * <p>允许为每个 {@link ShutdownPhase} 设置不同的超时时间。
     * 如果某个阶段没有配置，则使用 {@link #phaseTimeout} 作为默认值。
     *
     * <p>配置示例：
     * <pre>
     * wuli3.spring.shutdown.phases.DRAIN_ASYNC_TASKS.timeout=60s
     * </pre>
     */
    private Map<ShutdownPhase, PhaseProperties> phases = new EnumMap<>(ShutdownPhase.class);

    /**
     * 设置默认的阶段超时时间。
     *
     * @param phaseTimeout 超时时长，必须为正数
     * @throws IllegalArgumentException 如果超时时长为零或负数
     */
    public void setPhaseTimeout(final Duration phaseTimeout) {
        this.phaseTimeout = GracefulShutdownProperties.requirePositive(phaseTimeout, "phaseTimeout");
    }

    /**
     * 返回指定阶段的超时时间。
     *
     * <p>如果该阶段在 {@link #phases} 中有专用配置，则返回该配置的超时时间；
     * 否则返回 {@link #phaseTimeout} 作为默认值。
     *
     * @param phase 要查询的关闭阶段
     * @return 该阶段的超时时间
     * @throws NullPointerException 如果 phase 为 null
     */
    public Duration getTimeout(final ShutdownPhase phase) {
        final PhaseProperties phaseProperties = this.phases.get(Objects.requireNonNull(phase, "phase"));
        return phaseProperties == null ? this.phaseTimeout : phaseProperties.getTimeout();
    }

    public void setPhases(final Map<ShutdownPhase, PhaseProperties> phases) {
        this.phases = new EnumMap<>(Objects.requireNonNull(phases, "phases"));
    }

    /**
     * 验证 Duration 是否为正数。
     *
     * @param duration 要验证的时长
     * @param name 参数名称，用于异常消息
     * @return 验证通过的时长
     * @throws IllegalArgumentException 如果时长为零或负数
     */
    private static Duration requirePositive(final Duration duration, final String name) {
        final Duration actualDuration = Objects.requireNonNull(duration, name);
        if (actualDuration.isZero() || actualDuration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return actualDuration;
    }

    /**
     * 配置单个关闭阶段的属性。
     *
     * <p>目前仅支持配置超时时间，未来可能扩展其他属性。
     */
    @Getter
    public static final class PhaseProperties {

        /**
         * 该阶段的超时时间。
         *
         * <p>默认为 30 秒。支持的时间单位包括：ms、s、m、h。
         */
        @DurationUnit(SECONDS)
        private Duration timeout = Duration.ofSeconds(30);

        /**
         * 设置该阶段的超时时间。
         *
         * @param timeout 超时时长，必须为正数
         * @throws IllegalArgumentException 如果超时时长为零或负数
         */
        public void setTimeout(final Duration timeout) {
            this.timeout = GracefulShutdownProperties.requirePositive(timeout, "timeout");
        }
    }
}

package com.kjs.wuli3.spring.autoconfigure;

import com.kjs.wuli3.spring.shutdown.GracefulShutdownCoordinator;
import com.kjs.wuli3.spring.shutdown.RegisterShutdownHook;
import com.kjs.wuli3.spring.shutdown.ShutdownHook;
import com.kjs.wuli3.spring.shutdown.ShutdownHookAutoRegistrar;
import com.kjs.wuli3.spring.shutdown.ShutdownHookRegistry;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 配置公共优雅关闭基础设施。
 *
 * <p>此自动配置类注册了优雅关闭所需的核心组件：
 * <ul>
 *   <li>{@link ShutdownHookRegistry} - 钩子注册表</li>
 *   <li>{@link GracefulShutdownCoordinator} - 阶段协调器</li>
 * </ul>
 *
 * <h3>启用条件</h3>
 * <p>默认启用，可通过以下配置禁用：
 * <pre>
 * wuli3.spring.shutdown.enabled=false
 * </pre>
 *
 * <h3>配置属性</h3>
 * <p>支持通过 {@link GracefulShutdownProperties} 配置：
 * <ul>
 *   <li>{@code wuli3.spring.shutdown.phase-timeout} - 默认阶段超时时间（默认 30 秒）</li>
 *   <li>{@code wuli3.spring.shutdown.phases.<阶段>.timeout} - 特定阶段的超时时间</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * @RegisterShutdownHook(phase = ShutdownPhase.DRAIN_ASYNC_TASKS, priority = 100)
 * public class MyShutdownHook implements ShutdownHook {
 *
 *     @Override
 *     public void shutdown(ShutdownContext context) throws InterruptedException {
 *         // 执行关闭逻辑
 *     }
 * }
 * }</pre>
 *
 * @author GuoYang create on 2026/9/3 14:55
 * @see GracefulShutdownProperties
 * @see ShutdownHookRegistry
 * @see GracefulShutdownCoordinator
 */
@AutoConfiguration
@EnableConfigurationProperties(GracefulShutdownProperties.class)
@ConditionalOnProperty(prefix = "wuli3.spring.shutdown", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GracefulShutdownAutoConfiguration {

    /**
     * 注册关闭钩子注册表，用于存储和查询钩子。
     *
     * <p>钩子按阶段和优先级组织，关闭开始后不再接受新注册。
     *
     * @return 注册表实例
     */
    @Bean
    @ConditionalOnMissingBean
    ShutdownHookRegistry shutdownHookRegistry() {
        return new ShutdownHookRegistry();
    }

    /**
     * 注册关闭协调器，用于按顺序执行所有阶段。
     *
     * <p>协调器集成到 Spring 容器的生命周期中，并直接顺序执行各阶段钩子。
     * 当容器关闭时会自动触发协调器执行所有关闭阶段。
     *
     * @param registry 钩子注册表
     * @param properties 配置属性
     * @return 协调器实例
     */
    @Bean
    @ConditionalOnMissingBean
    GracefulShutdownCoordinator gracefulShutdownCoordinator(
            final ShutdownHookRegistry registry, final GracefulShutdownProperties properties) {
        return new GracefulShutdownCoordinator(registry, properties::getTimeout);
    }

    /**
     * 注册钩子自动注册器，自动收集并注册所有带 {@link RegisterShutdownHook} 注解的钩子。
     *
     * <p>这个 Bean 会在容器启动时扫描所有 {@link ShutdownHook} 实现，
     * 并将带有 {@link RegisterShutdownHook} 注解的钩子自动注册到对应的阶段。
     *
     * <p>这样各模块只需要：
     * <ol>
     *   <li>实现 {@link ShutdownHook} 接口</li>
     *   <li>添加 {@link RegisterShutdownHook} 注解声明阶段和优先级</li>
     *   <li>将钩子注册为 Spring Bean</li>
     * </ol>
     * 不需要手动调用 {@link ShutdownHookRegistry#register}。
     *
     * @param registry 钩子注册表
     * @param hooks 所有 ShutdownHook Bean（由 Spring 自动收集）
     * @return 自动注册器实例
     */
    @Bean
    @ConditionalOnMissingBean
    ShutdownHookAutoRegistrar shutdownHookAutoRegistrar(
            final ShutdownHookRegistry registry, final List<ShutdownHook> hooks) {
        return new ShutdownHookAutoRegistrar(registry, hooks);
    }
}

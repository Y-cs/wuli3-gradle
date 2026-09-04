package com.kjs.wuli3.spring.shutdown;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * 自动收集并注册所有带 {@link RegisterShutdownHook} 注解的钩子。
 *
 * <p>在 Spring 容器启动完成后，扫描所有实现了 {@link ShutdownHook} 接口且带有
 * {@link RegisterShutdownHook} 注解的 Bean，并根据注解声明的阶段和优先级
 * 自动注册到 {@link ShutdownHookRegistry}。
 *
 * <h3>工作流程</h3>
 * <ol>
 *   <li>Spring 容器完成所有 Bean 的初始化</li>
 *   <li>通过构造函数注入获取所有 {@link ShutdownHook} Bean</li>
 *   <li>检查每个钩子是否带有 {@link RegisterShutdownHook} 注解</li>
 *   <li>将带注解的钩子注册到对应的阶段</li>
 *   <li>记录注册日志，便于问题排查</li>
 * </ol>
 *
 * <h3>注册规则</h3>
 * <ul>
 *   <li>只注册带 {@link RegisterShutdownHook} 注解的钩子</li>
 *   <li>不带注解的钩子需要手动注册</li>
 *   <li>同一个钩子不会重复注册</li>
 *   <li>注册顺序不影响执行顺序（执行顺序由阶段和优先级决定）</li>
 * </ul>
 *
 * @author GuoYang create on 2026/9/4
 * @see RegisterShutdownHook
 * @see ShutdownHook
 * @see ShutdownHookRegistry
 */
public final class ShutdownHookAutoRegistrar implements SmartInitializingSingleton {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHookAutoRegistrar.class);

    private final ShutdownHookRegistry registry;
    private final List<ShutdownHook> hooks;

    /**
     * 创建自动注册器。
     *
     * <p>通过构造函数注入的方式，在所有 Bean 初始化完成后才会创建此实例，
     * 确保所有钩子都已经就绪。
     *
     * @param registry 钩子注册表
     * @param hooks 所有实现了 {@link ShutdownHook} 接口的 Bean
     */
    public ShutdownHookAutoRegistrar(final ShutdownHookRegistry registry, final List<ShutdownHook> hooks) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.hooks = List.copyOf(Objects.requireNonNull(hooks, "hooks"));
    }

    /**
     * 在所有单例完成初始化后统一注册钩子，确保拿到的是最终代理对象且避免构造阶段副作用。
     */
    @Override
    public void afterSingletonsInstantiated() {
        this.registerAnnotatedHooks(this.hooks);
    }

    /**
     * 扫描并注册所有带注解的钩子。
     *
     * @param hooks 候选钩子列表
     */
    private void registerAnnotatedHooks(final List<ShutdownHook> hooks) {
        if (hooks.isEmpty()) {
            ShutdownHookAutoRegistrar.LOGGER.debug("No ShutdownHook beans found in application context");
            return;
        }

        int registeredCount = 0;
        for (final ShutdownHook hook : hooks) {
            if (this.registerIfAnnotated(hook)) {
                registeredCount++;
            }
        }

        ShutdownHookAutoRegistrar.LOGGER.info(
                "Auto-registered {} shutdown hook(s) from @RegisterShutdownHook annotations", registeredCount);
    }

    /**
     * 检查钩子是否带有注解，如果有则注册。
     *
     * @param hook 待检查的钩子
     * @return 如果注册成功返回 true，否则返回 false
     */
    private boolean registerIfAnnotated(final ShutdownHook hook) {
        final Class<?> hookClass = hook.getClass();
        final RegisterShutdownHook annotation =
                AnnotatedElementUtils.findMergedAnnotation(hookClass, RegisterShutdownHook.class);

        if (annotation == null) {
            ShutdownHookAutoRegistrar.LOGGER.trace(
                    "ShutdownHook {} does not have @RegisterShutdownHook annotation, skipping auto-registration",
                    hookClass.getName());
            return false;
        }

        final ShutdownPhase phase = annotation.phase();
        final int priority = annotation.priority();

        this.registry.register(phase, hook, priority);

        ShutdownHookAutoRegistrar.LOGGER.debug(
                "Auto-registered shutdown hook: {} at phase={}, priority={}", hook.name(), phase, priority);

        return true;
    }
}

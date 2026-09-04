package com.kjs.wuli3.spring.shutdown;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个 {@link ShutdownHook} Bean 应该自动注册到优雅关闭流程。
 *
 * <p>使用此注解可以避免在各模块的自动配置中手动调用 {@link ShutdownHookRegistry#register}。
 * Spring 容器会自动发现所有带此注解的 Bean，并根据注解中声明的阶段和优先级进行注册。
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @RegisterShutdownHook(phase = ShutdownPhase.DRAIN_ASYNC_TASKS, priority = 100)
 * public class MyShutdownHook implements ShutdownHook {
 *     @Override
 *     public void shutdown(ShutdownContext context) {
 *         // 执行关闭逻辑
 *     }
 *
 *     @Override
 *     public String name() {
 *         return "my-component";
 *     }
 * }
 * }</pre>
 *
 * <h3>注册流程</h3>
 * <ol>
 *   <li>Spring 容器启动时扫描所有 Bean</li>
 *   <li>{@link ShutdownHookAutoRegistrar} 收集所有带此注解的 {@link ShutdownHook}</li>
 *   <li>根据注解声明的 {@link #phase()} 和 {@link #priority()} 自动注册</li>
 * </ol>
 *
 * <h3>与手动注册的关系</h3>
 * <p>使用此注解和手动调用 {@link ShutdownHookRegistry#register} 可以共存：
 * <ul>
 *   <li>推荐使用注解方式 - 声明式、解耦、易维护</li>
 *   <li>特殊场景可以使用手动注册 - 动态条件、运行时决策</li>
 * </ul>
 *
 * @author GuoYang create on 2026/9/4
 * @see ShutdownHook
 * @see ShutdownHookRegistry
 * @see ShutdownHookAutoRegistrar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterShutdownHook {

    /**
     * 指定钩子所属的关闭阶段。
     *
     * <p>阶段决定了钩子在整个关闭流程中的执行顺序，详见 {@link ShutdownPhase}。
     *
     * @return 关闭阶段
     */
    ShutdownPhase phase();

    /**
     * 指定钩子在同一阶段内的优先级。
     *
     * <p>数值越小越先执行。建议使用以下约定：
     * <ul>
     *   <li><b>-1000 ~ -100</b>: 必须最先执行（如停止组件内部的任务接收）</li>
     *   <li><b>-99 ~ 99</b>: 普通优先级（大部分钩子）</li>
     *   <li><b>100 ~ 1000</b>: 必须最后执行（如关闭监控）</li>
     * </ul>
     *
     * @return 优先级，默认为 0
     */
    int priority() default 0;
}

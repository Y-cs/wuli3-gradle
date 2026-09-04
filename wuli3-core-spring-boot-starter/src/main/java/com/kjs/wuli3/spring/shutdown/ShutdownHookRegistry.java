package com.kjs.wuli3.spring.shutdown;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 关闭钩子注册表，负责存储和查询钩子。
 *
 * <p>注册表是纯粹的存储层，不包含执行逻辑。钩子按阶段和优先级组织，
 * 提供线程安全的注册接口和只读的查询接口。
 *
 * <h3>注册规则</h3>
 * <ul>
 *   <li>同一阶段内，优先级数值较小的钩子先执行</li>
 *   <li>相同优先级的钩子按注册顺序执行</li>
 *   <li>关闭开始后（{@link #closeRegistration()}）不允许继续注册</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>注册和查询操作都是线程安全的。查询返回的列表是快照，不会被后续注册影响。
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class MyComponent {
 *     @Autowired
 *     public MyComponent(ShutdownHookRegistry registry) {
 *         registry.register(ShutdownPhase.DRAIN_ASYNC_TASKS,
 *                          context -> cleanup(),
 *                          100);
 *     }
 *
 *     private void cleanup() {
 *         // 清理逻辑
 *     }
 * }
 * }</pre>
 *
 * @author GuoYang create on 2026/9/4
 * @see ShutdownHook
 * @see ShutdownPhase
 */
public final class ShutdownHookRegistry {

    private static final Comparator<HookEntry> HOOK_ORDER =
            Comparator.comparingInt(HookEntry::priority).thenComparingLong(HookEntry::sequence);

    private final Map<ShutdownPhase, List<HookEntry>> hooks = new EnumMap<>(ShutdownPhase.class);
    private final AtomicLong registrationSequence = new AtomicLong();
    private final AtomicBoolean registrationOpen = new AtomicBoolean(true);

    /** 创建一个允许注册钩子的空注册表。 */
    public ShutdownHookRegistry() {
        for (final ShutdownPhase phase : ShutdownPhase.values()) {
            this.hooks.put(phase, new ArrayList<>());
        }
    }

    /**
     * 注册关闭钩子。
     *
     * <p>钩子会被分配到指定的关闭阶段，并按照优先级排序。
     * 相同优先级的钩子按照注册顺序执行。
     *
     * <h4>优先级建议</h4>
     * <ul>
     *   <li>0-99: 高优先级（关键资源，如健康检查端点）</li>
     *   <li>100-199: 正常优先级（业务逻辑）</li>
     *   <li>200+: 低优先级（清理任务）</li>
     * </ul>
     *
     * @param phase 钩子所属的关闭阶段
     * @param hook 要注册的关闭钩子
     * @param priority 执行优先级，数值越小越先执行
     * @throws IllegalStateException 如果注册已关闭（关闭流程已开始）
     * @throws NullPointerException 如果任何参数为 null
     */
    public synchronized void register(final ShutdownPhase phase, final ShutdownHook hook, final int priority) {
        if (!this.registrationOpen.get()) {
            throw new IllegalStateException("shutdown hook registry is closed");
        }
        final List<HookEntry> phaseHooks =
                Objects.requireNonNull(this.hooks.get(Objects.requireNonNull(phase, "phase")), "phase hooks");
        phaseHooks.add(new HookEntry(
                priority, this.registrationSequence.getAndIncrement(), Objects.requireNonNull(hook, "hook")));
        phaseHooks.sort(ShutdownHookRegistry.HOOK_ORDER);
    }

    /**
     * 查询指定阶段的所有钩子。
     *
     * <p>返回的列表已按优先级排序（升序），并且是不可变的快照。
     * 后续的注册操作不会影响已返回的列表。
     *
     * @param phase 要查询的关闭阶段
     * @return 该阶段的钩子列表，按优先级排序，如果该阶段没有钩子则返回空列表
     * @throws NullPointerException 如果 phase 为 null
     */
    public synchronized List<ShutdownHook> getHooks(final ShutdownPhase phase) {
        final List<HookEntry> phaseHooks =
                Objects.requireNonNull(this.hooks.get(Objects.requireNonNull(phase, "phase")), "phase hooks");
        return phaseHooks.stream().map(HookEntry::hook).toList();
    }

    /**
     * 检查是否仍允许注册新钩子。
     *
     * @return 如果仍可注册返回 {@code true}，否则返回 {@code false}
     */
    public boolean isRegistrationAllowed() {
        return this.registrationOpen.get();
    }

    /**
     * 关闭注册，禁止后续注册新钩子。
     *
     * <p>此方法通常在关闭流程开始时调用，确保关闭过程中不会有新钩子注册。
     * 关闭后的注册尝试会抛出 {@link IllegalStateException}。
     *
     * <p>此方法是幂等的，多次调用效果相同。
     */
    public synchronized void closeRegistration() {
        this.registrationOpen.set(false);
    }

    private record HookEntry(int priority, long sequence, ShutdownHook hook) {}
}

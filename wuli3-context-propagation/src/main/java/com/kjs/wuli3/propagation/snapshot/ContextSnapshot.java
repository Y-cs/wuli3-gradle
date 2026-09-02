package com.kjs.wuli3.propagation.snapshot;

import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.context.PropagationContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 可跨异步任务和协议边界传递的不可变上下文快照。
 *
 * <p>快照保存 {@link Context}。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ContextSnapshot {

    private static final ContextSnapshot EMPTY = new ContextSnapshot(Map.of());

    private final Map<Class<? extends Context>, Context> contexts;

    private ContextSnapshot(final Map<Class<? extends Context>, Context> contexts) {
        this.contexts = Map.copyOf(contexts);
    }

    /**
     * 返回不包含任何上下文的快照。
     *
     * @return 空上下文快照
     */
    public static ContextSnapshot empty() {
        return ContextSnapshot.EMPTY;
    }

    /**
     * 由指定上下文创建快照；同类型上下文以最后一个为准。
     *
     * @param contexts 待写入快照的上下文
     * @return 独立且不可变的上下文快照
     * @throws NullPointerException 当上下文数组或任一上下文为 {@code null} 时
     */
    public static ContextSnapshot of(final Context... contexts) {
        Objects.requireNonNull(contexts, "contexts");
        if (contexts.length == 0) {
            return ContextSnapshot.empty();
        }
        final Map<Class<? extends Context>, Context> snapshotContexts = new HashMap<>();
        for (final Context context : contexts) {
            final Context actualContext = Objects.requireNonNull(context, "context");
            final Class<? extends Context> type = Objects.requireNonNull(actualContext.type(), "context.type()");
            snapshotContexts.put(type, actualContext);
        }
        return new ContextSnapshot(snapshotContexts);
    }

    /**
     * 由容器内部映射直接构建快照，仅保留可传播上下文；单次遍历完成过滤与拷贝。
     *
     * @param rawContexts 容器内部的完整上下文映射
     * @return 仅包含 {@link PropagationContext} 的独立快照
     */
    public static ContextSnapshot ofPropagationOnly(final Map<Class<? extends Context>, Context> rawContexts) {
        if (rawContexts.isEmpty()) {
            return ContextSnapshot.EMPTY;
        }
        final Map<Class<? extends Context>, Context> filtered = new HashMap<>(rawContexts.size());
        for (final Map.Entry<Class<? extends Context>, Context> entry : rawContexts.entrySet()) {
            if (entry.getValue() instanceof PropagationContext) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered.isEmpty() ? ContextSnapshot.EMPTY : new ContextSnapshot(filtered);
    }

    /**
     * 获取指定类型的上下文。
     *
     * @param type 要获取的上下文类型
     * @param <T>  上下文具体类型
     * @return 对应上下文；快照中不存在时为空
     * @throws NullPointerException 当 {@code type} 为 {@code null} 时
     */
    public <T extends Context> Optional<T> get(final Class<T> type) {
        final Class<T> actualType = Objects.requireNonNull(type, "type");
        return Optional.ofNullable(actualType.cast(this.contexts.get(actualType)));
    }

    /**
     * 返回快照中全部上下文的不可变集合。
     *
     * @return 全部上下文
     */
    public Collection<Context> values() {
        return this.contexts.values();
    }

    /**
     * 判断快照是否不包含任何上下文。
     *
     * @return 当快照为空时为 {@code true}
     */
    public boolean isEmpty() {
        return this.contexts.isEmpty();
    }
}

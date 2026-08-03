package com.kjs.wuli3.propagation.snapshot;

import com.kjs.wuli3.propagation.context.Context;

import java.util.*;

/**
 * 可跨异步任务和协议边界传递的不可变上下文快照。
 *
 * <p>快照保存 {@link Context}。
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

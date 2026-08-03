package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;

import java.util.*;

/**
 * 按具体上下文类型保存当前执行上下文的可变容器。
 *
 * <p>该容器不保证线程安全，应由 {@link ContextStore} 或调用方负责线程隔离。
 */
public final class ContextContainer {
    private final Map<Class<? extends Context>, Context> contexts;

    /**
     * 创建不包含任何上下文的容器。
     */
    public ContextContainer() {
        this(new HashMap<>());
    }

    /**
     * 基于给定映射创建独立容器副本。
     */
    private ContextContainer(final Map<Class<? extends Context>, Context> contexts) {
        this.contexts = new HashMap<>(contexts);
    }

    /**
     * 按上下文具体类型存入一个上下文；已有同类型上下文会被替换。
     *
     * @param context 待存入的上下文
     * @param <T>     上下文具体类型
     * @throws NullPointerException 当 {@code context} 为 {@code null} 时
     */
    public <T extends Context> void put(final T context) {
        final T actualContext = Objects.requireNonNull(context, "context");
        final Class<? extends Context> type = Objects.requireNonNull(actualContext.type(), "context.type()");
        this.contexts.put(type, actualContext);
    }

    /**
     * 按具体类型获取上下文。
     *
     * @param type 要获取的上下文类型
     * @param <T>  上下文具体类型
     * @return 对应上下文；不存在时为空
     */
    public <T extends Context> Optional<T> get(final Class<T> type) {
        final Class<T> actualType = Objects.requireNonNull(type, "type");
        return Optional.ofNullable(actualType.cast(this.contexts.get(actualType)));
    }

    /**
     * 删除指定类型的上下文；类型不存在时不产生影响。
     *
     * @param type 待删除的上下文类型
     */
    public void remove(final Class<? extends Context> type) {
        this.contexts.remove(Objects.requireNonNull(type, "type"));
    }

    /**
     * 返回当前全部上下文的不可变快照。
     *
     * @return 不可修改的上下文集合
     */
    public Collection<Context> values() {
        return Map.copyOf(this.contexts)
                .values();
    }

    /**
     * 捕获容器中允许跨边界传播的上下文。
     *
     * <p>普通 {@link Context} 不会进入返回快照。
     *
     * @return 当前容器的传播上下文快照
     */
    public ContextSnapshot capture() {
        return ContextSnapshot.of(this.contexts.values()
                .toArray(Context[]::new));
    }
}

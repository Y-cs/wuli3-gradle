package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;

import java.util.Objects;
import java.util.Optional;

/**
 * 基于 {@link ThreadLocal} 保存当前调用完整上下文的存储。
 *
 * <p>读取不会创建容器；首次写入时才将新容器绑定到当前线程。
 */
public final class ContextStore implements ContextReader, ContextWriter {

    private final ThreadLocal<ContextContainer> holder = new ThreadLocal<>();

    /**
     * 获取当前线程中指定类型的上下文，不会创建新的容器。
     *
     * @param type 要获取的上下文类型
     * @param <T>  上下文具体类型
     * @return 对应上下文；当前线程未设置时为空
     */
    @Override
    public <T extends Context> Optional<T> get(final Class<T> type) {
        final ContextContainer current = this.holder.get();
        return current == null ? Optional.empty() : current.get(type);
    }

    /**
     * 将上下文存入当前线程；当前线程尚未绑定容器时会先创建容器。
     *
     * @param context 待存入的上下文
     * @param <T>     上下文具体类型
     */
    @Override
    public <T extends Context> void put(final T context) {
        this.current()
                .put(context);
    }

    /**
     * 从当前线程容器中删除指定类型的上下文。
     *
     * @param type 待删除的上下文类型
     */
    @Override
    public void remove(final Class<? extends Context> type) {
        final Class<? extends Context> actualType = Objects.requireNonNull(type, "type");
        final ContextContainer current = this.holder.get();
        if (current != null) {
            current.remove(actualType);
        }
    }

    /**
     * 捕获当前线程中全部可传播上下文的独立快照。
     *
     * @return 当前线程的传播上下文快照；当前线程未绑定容器时为空快照
     */
    @Override
    public ContextSnapshot capture() {
        final ContextContainer current = this.holder.get();
        return current == null ? ContextSnapshot.empty() : current.capture();
    }

    /**
     * 临时恢复传播上下文快照，并在作用域关闭时恢复之前的完整本地上下文。
     *
     * @param snapshot 待恢复的传播上下文快照
     * @return 用于恢复先前完整上下文的作用域
     * @throws NullPointerException 当 {@code snapshot} 为 {@code null} 时
     */
    @Override
    public ContextScope restore(final ContextSnapshot snapshot) {
        final ContextSnapshot actualSnapshot = Objects.requireNonNull(snapshot, "snapshot");
        final ContextContainer previous = this.holder.get();
        final ContextContainer restored = new ContextContainer();
        actualSnapshot.values()
                .forEach(restored::put);
        this.holder.set(restored);
        return () -> {
            if (previous == null) {
                this.holder.remove();
            } else {
                this.holder.set(previous);
            }
        };
    }

    /**
     * 从当前线程移除已绑定的上下文容器。
     */
    @Override
    public void clear() {
        this.holder.remove();
    }

    /**
     * 获取当前线程容器；不存在时创建并绑定一个空容器。
     */
    private ContextContainer current() {
        final ContextContainer current = this.holder.get();
        if (current == null) {
            final ContextContainer created = new ContextContainer();
            this.holder.set(created);
            return created;
        }
        return current;
    }
}

package com.kjs.wuli3.propagation;

import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.propagation.store.ContextWriter;

import java.util.Objects;

/**
 * 基于 {@link ContextReader} 和 {@link ContextWriter} 捕获和恢复调用上下文的默认实现。
 */
public final class DefaultContextPropagator implements ContextPropagator {

    private final ContextReader contextReader;
    private final ContextWriter contextWriter;

    /**
     * 基于同一个上下文存储创建传播器。
     *
     * @param contextStore 当前调用上下文存储
     * @throws NullPointerException 当 {@code contextStore} 为 {@code null} 时
     */
    public DefaultContextPropagator(final ContextStore contextStore) {
        this(Objects.requireNonNull(contextStore, "contextStore"), contextStore);
    }

    /**
     * 基于独立的读取与写入能力创建传播器。
     *
     * @param contextReader 当前调用上下文读取能力
     * @param contextWriter 当前调用上下文写入与恢复能力
     * @throws NullPointerException 当任一参数为 {@code null} 时
     */
    public DefaultContextPropagator(final ContextReader contextReader, final ContextWriter contextWriter) {
        this.contextReader = Objects.requireNonNull(contextReader, "contextReader");
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
    }

    /**
     * 捕获当前调用上下文；未绑定上下文时返回空快照。
     *
     * @return 独立的调用上下文快照
     */
    @Override
    public ContextSnapshot capture() {
        return this.contextReader.capture();
    }

    /**
     * 恢复调用上下文，并在作用域关闭时恢复先前上下文。
     *
     * @param snapshot 待恢复的上下文快照
     * @return 用于恢复先前上下文的作用域
     * @throws NullPointerException 当 {@code snapshot} 为 {@code null} 时
     */
    @Override
    public ContextScope restore(final ContextSnapshot snapshot) {
        return this.contextWriter.restore(Objects.requireNonNull(snapshot, "snapshot"));
    }
}

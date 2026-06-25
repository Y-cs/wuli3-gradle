package com.kjs.wuli3.propagation.propagation;

import com.kjs.wuli3.propagation.holder.ContextHolder;
import com.kjs.wuli3.propagation.holder.ContextHolderOperator;
import com.kjs.wuli3.propagation.holder.ContextContainer;

import java.util.Objects;

/**
 * 基于ContextHolder的默认上下文传播器。
 */
public final class DefaultContextPropagator implements ContextPropagator {

    private final ContextHolderOperator contextHolderOperator;

    public DefaultContextPropagator(ContextHolder holder) {
        this.contextHolderOperator = new ContextHolderOperator(Objects.requireNonNull(holder, "holder"));
    }

    @Override
    public ContextSnapshot capture() {
        return new ContextSnapshot(contextHolderOperator.current()
                .copy());
    }

    @Override
    public ContextScope restore(ContextSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        boolean hadPrevious = contextHolderOperator.hasCurrent();
        ContextContainer previous = contextHolderOperator.current();
        contextHolderOperator.restore(snapshot.getContextContainer()
                .copy());
        return () -> {
            if (hadPrevious) {
                contextHolderOperator.restore(previous);
            } else {
                contextHolderOperator.clear();
            }
        };
    }
}

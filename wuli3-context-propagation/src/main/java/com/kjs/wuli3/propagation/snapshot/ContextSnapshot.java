package com.kjs.wuli3.propagation.snapshot;

import com.kjs.wuli3.propagation.store.ContextContainer;

import java.util.Objects;

/**
 * 可在后续流程中恢复的调用上下文快照。
 */
public final class ContextSnapshot {

    private final ContextContainer contextContainer;

    public ContextSnapshot(final ContextContainer contextContainer) {
        this.contextContainer = Objects.requireNonNull(contextContainer, "contextContainer")
                .copy();
    }

    public ContextContainer getContextContainer() {
        return this.contextContainer.copy();
    }
}

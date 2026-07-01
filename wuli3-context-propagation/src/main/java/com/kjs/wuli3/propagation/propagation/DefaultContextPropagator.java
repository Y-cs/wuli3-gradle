package com.kjs.wuli3.propagation.propagation;

import com.kjs.wuli3.propagation.holder.ContextContainer;
import com.kjs.wuli3.propagation.holder.ContextWriter;

import java.util.Objects;

public final class DefaultContextPropagator implements ContextPropagator {

    private final ContextWriter contextWriter;

    public DefaultContextPropagator(ContextWriter contextWriter) {
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
    }

    @Override
    public ContextSnapshot capture() {
        ContextContainer current = contextWriter.snapshot();
        return new ContextSnapshot(current != null ? current.copy() : new ContextContainer());
    }

    @Override
    public ContextScope restore(ContextSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        ContextContainer previous = contextWriter.snapshot();
        contextWriter.restore(snapshot.getContextContainer().copy());
        return () -> {
            if (previous != null) {
                contextWriter.restore(previous);
            } else {
                contextWriter.clear();
            }
        };
    }
}

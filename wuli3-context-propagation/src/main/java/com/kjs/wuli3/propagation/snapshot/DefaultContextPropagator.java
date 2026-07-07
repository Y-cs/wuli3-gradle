package com.kjs.wuli3.propagation.snapshot;

import com.kjs.wuli3.propagation.store.ContextContainer;
import com.kjs.wuli3.propagation.store.ContextWriter;
import java.util.Objects;

public final class DefaultContextPropagator implements ContextPropagator {

    private final ContextWriter contextWriter;

    public DefaultContextPropagator(final ContextWriter contextWriter) {
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
    }

    @Override
    public ContextSnapshot capture() {
        final ContextContainer current = this.contextWriter.snapshot();
        return new ContextSnapshot(current != null ? current.copy() : new ContextContainer());
    }

    @Override
    public ContextScope restore(final ContextSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        final ContextContainer previous = this.contextWriter.snapshot();
        this.contextWriter.restore(snapshot.getContextContainer());
        return () -> {
            if (previous != null) {
                this.contextWriter.restore(previous);
            } else {
                this.contextWriter.clear();
            }
        };
    }
}

package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.context.Context;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Thread-local storage for the current invocation context.
 */
@SuppressWarnings("ThreadLocalUsage")
public final class ContextStore implements ContextReader, ContextWriter {

    private final ThreadLocal<ContextContainer> holder = new ThreadLocal<>();

    @Override
    public <T extends Context> Optional<T> get(Class<T> type) {
        ContextContainer c = holder.get();
        return c == null ? Optional.empty() : c.get(type);
    }

    @Override
    public <T extends Context> void put(T context) {
        current().put(context);
    }

    @Override
    public void remove(Class<? extends Context> type) {
        current().remove(type);
    }

    @Override
    public @Nullable ContextContainer snapshot() {
        final ContextContainer current = this.holder.get();
        return current == null ? null : current.copy();
    }

    @Override
    public void restore(final ContextContainer snapshot) {
        this.holder.set(snapshot.copy());
    }

    @Override
    public void clear() {
        this.holder.remove();
    }

    private ContextContainer current() {
        ContextContainer current = this.holder.get();
        if (current == null) {
            current = new ContextContainer();
            this.holder.set(current);
        }
        return current;
    }
}

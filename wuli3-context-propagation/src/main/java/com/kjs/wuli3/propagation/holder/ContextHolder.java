package com.kjs.wuli3.propagation.holder;

import com.kjs.wuli3.propagation.context.Context;

import java.util.Optional;

public final class ContextHolder implements ContextReader, ContextWriter {

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
    public ContextContainer snapshot() {
        return holder.get();
    }

    @Override
    public void restore(ContextContainer snapshot) {
        holder.set(snapshot);
    }

    @Override
    public void clear() {
        holder.remove();
    }

    private ContextContainer current() {
        ContextContainer c = holder.get();
        if (c == null) {
            c = new ContextContainer();
            holder.set(c);
        }
        return c;
    }
}

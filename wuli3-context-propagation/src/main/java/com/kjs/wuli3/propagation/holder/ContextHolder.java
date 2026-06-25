package com.kjs.wuli3.propagation.holder;

import com.kjs.wuli3.propagation.context.Context;

import java.util.Optional;


/**
 * ContextHolder
 *
 * @author GuoYang create on 2026/6/25 14:30
 */
public final class ContextHolder {

    private final ThreadLocal<ContextContainer> holder = new ThreadLocal<>();

    public <T extends Context> Optional<T> get(Class<T> type) {
        ContextContainer current = holder.get();
        if (current == null) {
            return Optional.empty();
        }
        return current.get(type);
    }

    ContextContainer currentContext() {
        ContextContainer current = this.holder.get();
        if (current == null) {
            current = new ContextContainer();
            this.holder.set(current);
        }
        return current;
    }

    boolean hasCurrentContext() {
        return this.holder.get() != null;
    }

    void restore(ContextContainer newHolder) {
        this.holder.set(newHolder);
    }

    void clear() {
        this.holder.remove();
    }
}

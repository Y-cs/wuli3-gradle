package com.kjs.wuli3.propagation.store;

import com.google.common.collect.Maps;
import com.kjs.wuli3.propagation.context.Context;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * ContextContainer
 *
 * @author GuoYang create on 2026/6/25 15:33
 */
public class ContextContainer {
    private final Map<Class<? extends Context>, Context> contexts = Maps.newConcurrentMap();

    public ContextContainer() {}

    private ContextContainer(final Map<Class<? extends Context>, Context> contexts) {
        this.contexts.putAll(contexts);
    }

    public <T extends Context> void put(final T context) {
        this.contexts.put(context.type(), context);
    }

    public <T extends Context> Optional<T> get(final Class<T> type) {
        return Optional.ofNullable(type.cast(this.contexts.get(type)));
    }

    public void remove(final Class<? extends Context> type) {
        this.contexts.remove(type);
    }

    public void clear() {
        this.contexts.clear();
    }

    public Collection<Context> values() {
        return Map.copyOf(this.contexts).values();
    }

    public ContextContainer copy() {
        return new ContextContainer(Map.copyOf(this.contexts));
    }
}

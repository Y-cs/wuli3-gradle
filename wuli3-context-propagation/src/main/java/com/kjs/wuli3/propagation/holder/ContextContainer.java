package com.kjs.wuli3.propagation.holder;

import com.google.common.collect.Maps;
import com.kjs.wuli3.propagation.context.Context;

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

    private ContextContainer(Map<Class<? extends Context>, Context> contexts) {
        this.contexts.putAll(contexts);
    }

    public <T extends Context> void put(T context) {
        contexts.put(context.type(), context);
    }

    public <T extends Context> Optional<T> get(Class<T> type) {
        return Optional.ofNullable(type.cast(contexts.get(type)));
    }

    public void remove(Class<? extends Context> type) {
        contexts.remove(type);
    }

    public void clear() {
        contexts.clear();
    }

    public ContextContainer copy() {
        return new ContextContainer(Map.copyOf(contexts));
    }
}

package com.kjs.wuli3.propagation.context;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

/**
 * AbstractContext
 *
 * @author GuoYang create on 2026/6/25 15:19
 */
public abstract class AbstractContext implements ExtendableContext {

    private final Map<ContextKey<?>, Object> extensions = Maps.newHashMap();

    @Override
    public <T> void put(ContextKey<T> key, T value) {
        extensions.put(key, value);
    }

    @Override
    public <T> Optional<T> get(ContextKey<T> key) {
        Class<T> type = key.type();
        return Optional.ofNullable(type.cast(extensions.get(key)));
    }
}

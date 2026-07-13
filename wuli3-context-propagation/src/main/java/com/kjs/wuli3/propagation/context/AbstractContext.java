package com.kjs.wuli3.propagation.context;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AbstractContext
 *
 * @author GuoYang create on 2026/6/25 15:19
 */
public abstract class AbstractContext implements ExtendableContext {

    private final Map<ContextKey<?>, Object> extensions;

    protected AbstractContext() {
        this.extensions = new ConcurrentHashMap<>();
    }

    protected AbstractContext(final Map<ContextKey<?>, Object> extensions) {
        this.extensions = new ConcurrentHashMap<>(Objects.requireNonNull(extensions, "extensions"));
    }

    @Override
    public <T> void put(final ContextKey<T> key, final T value) {
        Objects.requireNonNull(key, "key");
        this.extensions.put(key, Objects.requireNonNull(value, "value"));
    }

    @Override
    public <T> Optional<T> get(final ContextKey<T> key) {
        Objects.requireNonNull(key, "key");
        final Class<T> type = key.type();
        return Optional.ofNullable(type.cast(this.extensions.get(key)));
    }

    /** Returns a structural copy of extension entries; extension values themselves must be immutable. */
    protected final Map<ContextKey<?>, Object> extensionSnapshot() {
        return Map.copyOf(this.extensions);
    }
}

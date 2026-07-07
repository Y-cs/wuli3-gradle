package com.kjs.wuli3.propagation.context;

import java.util.Objects;

/**
 * ContextKey
 *
 * @author GuoYang create on 2026/6/25 15:18
 */
public record ContextKey<T>(String name, Class<T> type) {

    public ContextKey {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}

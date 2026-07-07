package com.kjs.wuli3.propagation.context;

import java.util.Optional;

/**
 * ExtendableContext
 *
 * @author GuoYang create on 2026/6/25 15:28
 */
public non-sealed interface ExtendableContext extends Context {

    <T> void put(ContextKey<T> key, T value);

    <T> Optional<T> get(ContextKey<T> key);
}

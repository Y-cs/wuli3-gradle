package com.kjs.wuli3.core.id;

/**
 * Generates identifiers without coupling callers to a concrete allocation algorithm.
 *
 * @param <T> identifier value type
 */
@FunctionalInterface
public interface IdGenerator<T> {

    T nextId();

    default String nextIdStr(String prefix) {
        return prefix + nextId();
    }
}

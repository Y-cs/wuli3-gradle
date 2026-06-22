package com.kjs.wuli3.core.function;

@FunctionalInterface
public interface CheckedConsumer<T> {
    void accept(T value) throws Exception;
}

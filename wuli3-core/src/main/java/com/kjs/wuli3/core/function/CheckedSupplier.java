package com.kjs.wuli3.core.function;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;
}

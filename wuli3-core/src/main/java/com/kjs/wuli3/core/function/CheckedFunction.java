package com.kjs.wuli3.core.function;

@FunctionalInterface
public interface CheckedFunction<T, R> {
    R apply(T value) throws Exception;
}

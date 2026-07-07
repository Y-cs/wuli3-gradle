package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.context.Context;

import java.util.Optional;

public interface ContextReader {
    <T extends Context> Optional<T> get(Class<T> type);
}

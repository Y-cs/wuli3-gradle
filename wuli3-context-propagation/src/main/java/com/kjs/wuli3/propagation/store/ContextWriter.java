package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.context.Context;
import org.jspecify.annotations.Nullable;

public interface ContextWriter {
    <T extends Context> void put(T context);

    void remove(Class<? extends Context> type);
    /** Returns an isolated copy of the current container, or null if none exists. */
    @Nullable
    ContextContainer snapshot();

    void restore(ContextContainer snapshot);

    void clear();
}

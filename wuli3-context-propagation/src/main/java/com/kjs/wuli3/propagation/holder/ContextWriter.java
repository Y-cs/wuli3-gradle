package com.kjs.wuli3.propagation.holder;

import com.kjs.wuli3.propagation.context.Context;

public interface ContextWriter {
    <T extends Context> void put(T context);
    void remove(Class<? extends Context> type);
    /** Returns the current container, or null if none exists. */
    ContextContainer snapshot();
    void restore(ContextContainer snapshot);
    void clear();
}

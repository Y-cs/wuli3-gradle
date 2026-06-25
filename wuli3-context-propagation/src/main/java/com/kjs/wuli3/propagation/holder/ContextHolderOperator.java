package com.kjs.wuli3.propagation.holder;

import com.kjs.wuli3.propagation.context.Context;

/**
 * ContextEditor
 *
 * @author GuoYang create on 2026/6/25 16:32
 */
public class ContextHolderOperator {

    private final ContextHolder contextHolder;

    public ContextHolderOperator(ContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public ContextContainer current() {
        return contextHolder.currentContext();
    }

    public boolean hasCurrent() {
        return contextHolder.hasCurrentContext();
    }

    public void restore(ContextContainer newHolder) {
        contextHolder.restore(newHolder);
    }

    public void clear() {
        contextHolder.clear();
    }

    public void remove(Class<? extends Context> type) {
        contextHolder.currentContext()
                .remove(type);
    }

    public <T extends Context> void put(T context) {
        contextHolder.currentContext()
                .put(context);
    }

}

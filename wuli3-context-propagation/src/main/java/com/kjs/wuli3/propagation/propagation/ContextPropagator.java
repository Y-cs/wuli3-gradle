package com.kjs.wuli3.propagation.propagation;

/**
 * 捕获并恢复当前调用上下文。
 */
public interface ContextPropagator {

    ContextSnapshot capture();

    ContextScope restore(ContextSnapshot snapshot);
}

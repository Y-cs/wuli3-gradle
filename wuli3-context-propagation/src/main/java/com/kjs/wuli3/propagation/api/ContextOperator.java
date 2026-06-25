package com.kjs.wuli3.propagation.api;

import com.kjs.wuli3.propagation.context.Context;

/**
 * 将协议相关的源对象解析为上下文模型。
 */
public interface ContextOperator<S, C extends Context> {

    @FunctionalInterface
    interface Resolver<S, C extends Context> {
        C resolve(S source);
    }

    @FunctionalInterface
    interface Injector<T, C extends Context> {
        void inject(C context, T target);
    }
}

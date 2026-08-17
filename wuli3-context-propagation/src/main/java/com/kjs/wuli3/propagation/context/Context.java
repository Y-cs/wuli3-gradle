package com.kjs.wuli3.propagation.context;

/**
 * 当前执行中可存储的上下文。
 *
 * <p>
 * 普通上下文只在当前执行范围内有效；只有 {@link PropagationContext} 才能进入跨异步任务和协议边界的快照。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface Context {

    /**
     * 返回当前上下文的具体类型，供上下文容器作为唯一键存取。
     *
     * @return 当前上下文的具体类型
     */
    Class<? extends Context> type();
}

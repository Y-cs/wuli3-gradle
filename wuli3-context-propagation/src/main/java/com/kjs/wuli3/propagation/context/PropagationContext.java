package com.kjs.wuli3.propagation.context;

/**
 * 可跨调用边界传播的固定上下文类型集合。
 *
 * <p>仅当存在明确的传播契约时才应新增实现类型。实现必须是不可变值对象，确保捕获快照后其内容不会随原上下文变化。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface PropagationContext extends Context {

    /**
     * 返回当前上下文的具体类型，供上下文容器作为唯一键存取。
     *
     * @return 当前上下文的具体类型
     */
    @Override
    Class<? extends PropagationContext> type();
}

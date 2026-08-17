package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;

/**
 * 提供当前调用上下文的可变访问。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface ContextWriter {

    /**
     * 按上下文具体类型存入上下文；已有同类型上下文会被替换。
     *
     * @param context 待存入的上下文
     * @param <T>     上下文具体类型
     */
    <T extends Context> void put(final T context);

    /**
     * 删除指定类型的上下文；类型不存在时不产生影响。
     *
     * @param type 待删除的上下文类型
     */
    void remove(final Class<? extends Context> type);

    /**
     * 在当前调用中临时恢复传播上下文快照。
     *
     * <p>关闭返回作用域后，会恢复进入作用域前的完整本地上下文。
     *
     * @param snapshot 待恢复的传播上下文快照
     * @return 用于恢复先前上下文的作用域
     */
    ContextScope restore(final ContextSnapshot snapshot);

    /**
     * 清除当前调用已绑定的全部上下文。
     */
    void clear();
}

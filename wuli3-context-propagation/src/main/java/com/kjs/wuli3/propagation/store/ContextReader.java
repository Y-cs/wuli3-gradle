package com.kjs.wuli3.propagation.store;

import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.Optional;

/** 提供当前调用上下文的只读访问。 */
public interface ContextReader {

    /**
     * 获取当前调用中指定类型的上下文。
     *
     * @param type 要获取的上下文类型
     * @param <T> 上下文具体类型
     * @return 对应上下文；当前调用未设置时为空
     */
    <T extends Context> Optional<T> get(final Class<T> type);

    /**
     * 捕获当前调用中全部可传播上下文的独立快照。
     *
     * @return 当前调用的传播上下文快照；未设置上下文时为空快照
     */
    ContextSnapshot capture();
}

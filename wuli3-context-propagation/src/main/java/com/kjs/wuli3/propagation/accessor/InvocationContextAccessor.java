package com.kjs.wuli3.propagation.accessor;

import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/** 提供当前调用中请求上下文的便捷只读访问。 */
@RequiredArgsConstructor
public class InvocationContextAccessor {

    private final ContextReader contextReader;

    /**
     * 获取当前调用的完整请求上下文。
     *
     * @return 当前请求上下文；未设置时为空
     */
    public Optional<InvocationContext> current() {
        return this.contextReader.get(InvocationContext.class);
    }

    /**
     * 获取用于关联调用链路的请求标识。
     *
     * @return 当前请求 ID；未设置请求上下文时为空
     */
    public Optional<String> requestId() {
        return this.current().map(InvocationContext::requestId);
    }

    /**
     * 获取当前调用来源的 IP 地址。
     *
     * @return 来源 IP；未设置请求上下文时为空
     */
    public Optional<String> originIp() {
        return this.current().map(InvocationContext::originIp);
    }
}

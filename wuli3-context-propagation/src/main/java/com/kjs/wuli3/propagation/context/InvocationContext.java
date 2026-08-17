package com.kjs.wuli3.propagation.context;

/**
 * 调用链中需要传播的请求元数据。
 *
 * @param originIp 调用来源的 IP 地址
 * @param requestId 用于关联调用链路的请求标识
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record InvocationContext(String originIp, String requestId) implements PropagationContext {

    /**
     * 返回调用上下文的类型，用作上下文容器中的存取键。
     *
     * @return {@link InvocationContext} 的类型
     */
    @Override
    public Class<? extends PropagationContext> type() {
        return InvocationContext.class;
    }
}

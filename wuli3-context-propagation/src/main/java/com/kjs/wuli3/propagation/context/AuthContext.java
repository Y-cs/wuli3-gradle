package com.kjs.wuli3.propagation.context;

/**
 * 业务代码可读取的认证与授权元数据。
 *
 * @param userId 已认证用户的唯一标识
 * @param username 已认证用户的用户名
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuthContext(Long userId, String username) implements PropagationContext {

    /**
     * 返回认证上下文的类型，用作上下文容器中的存取键。
     *
     * @return {@link AuthContext} 的类型
     */
    @Override
    public Class<? extends PropagationContext> type() {
        return AuthContext.class;
    }
}

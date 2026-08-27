package com.kjs.wuli3.propagation.context;

import java.util.Objects;

/**
 * 业务代码可读取的认证与授权元数据。
 *
 * @param principalType 认证主体类型
 * @param principalId 认证主体的唯一标识
 * @param principalName 认证主体的显示名称
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuthContext(PrincipalType principalType, String principalId, String principalName)
        implements PropagationContext {

    /** 创建字段完整的认证主体快照。 */
    public AuthContext {
        Objects.requireNonNull(principalType, "principalType");
        AuthContext.requireNonBlank(principalId, "principalId");
        AuthContext.requireNonBlank(principalName, "principalName");
    }

    /**
     * 返回认证上下文的类型，用作上下文容器中的存取键。
     *
     * @return {@link AuthContext} 的类型
     */
    @Override
    public Class<? extends PropagationContext> type() {
        return AuthContext.class;
    }

    private static void requireNonBlank(final String value, final String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}

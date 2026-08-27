package com.kjs.wuli3.propagation.accessor;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/** 提供当前调用中认证上下文的便捷只读访问。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@RequiredArgsConstructor
public class AuthContextAccessor {

    private final ContextReader contextReader;

    /**
     * 获取当前调用的完整认证上下文。
     *
     * @return 当前认证上下文；未设置时为空
     */
    public Optional<AuthContext> current() {
        return this.contextReader.get(AuthContext.class);
    }

    /**
     * 获取当前认证主体的类型。
     *
     * @return 当前主体类型；未设置认证上下文时为空
     */
    public Optional<PrincipalType> principalType() {
        return this.current().map(AuthContext::principalType);
    }

    /**
     * 获取当前认证主体的唯一标识。
     *
     * @return 当前主体 ID；未设置认证上下文时为空
     */
    public Optional<String> principalId() {
        return this.current().map(AuthContext::principalId);
    }

    /**
     * 获取当前认证主体的显示名称。
     *
     * @return 当前主体名称；未设置认证上下文时为空
     */
    public Optional<String> principalName() {
        return this.current().map(AuthContext::principalName);
    }
}

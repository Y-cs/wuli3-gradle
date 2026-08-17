package com.kjs.wuli3.propagation.accessor;

import com.kjs.wuli3.propagation.context.AuthContext;
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
     * 获取当前认证用户的唯一标识。
     *
     * @return 当前用户 ID；未设置认证上下文时为空
     */
    public Optional<Long> userId() {
        return this.current().map(AuthContext::userId);
    }

    /**
     * 获取当前认证用户的用户名。
     *
     * @return 当前用户名；未设置认证上下文时为空
     */
    public Optional<String> username() {
        return this.current().map(AuthContext::username);
    }
}

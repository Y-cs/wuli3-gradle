package com.kjs.wuli3.web.auth;

import com.kjs.wuli3.propagation.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 从入站 HTTP 请求解析认证上下文。
 *
 * <p>默认实现恢复可信内部 HTTP 调用携带的认证字段。直接接收外部请求或使用其他认证协议的应用，
 * 应提供自己的实现，并在返回 {@link AuthContext} 前完成相应的身份认证。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface AuthContextResolver {

    /**
     * 解析当前请求对应的认证上下文。
     *
     * @param request 当前 HTTP 请求
     * @return 已认证的上下文；请求不包含有效认证信息时为空
     */
    Optional<AuthContext> resolve(final HttpServletRequest request);
}

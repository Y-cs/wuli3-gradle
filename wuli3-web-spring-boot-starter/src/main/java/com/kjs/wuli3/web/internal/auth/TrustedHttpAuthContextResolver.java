package com.kjs.wuli3.web.internal.auth;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.encoding.AuthContextEncoder;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

/**
 * 从可信内部 HTTP 请求头恢复认证上下文。
 *
 * <p>该实现假定上游网关已经完成身份认证，并按照 wuli3 上下文传播协议写入认证字段。直接接收外部请求或使用其他认证协议的应用，
 * 应提供自己的 {@link AuthContextResolver} Bean 替换本实现。
 */
public final class TrustedHttpAuthContextResolver implements AuthContextResolver {

    private final AuthContextEncoder authContextEncoder = new AuthContextEncoder();

    @Override
    public Optional<AuthContext> resolve(final HttpServletRequest request) {
        final HttpServletRequest actualRequest = Objects.requireNonNull(request, "request");
        return this.authContextEncoder.decode(actualRequest::getHeader);
    }
}

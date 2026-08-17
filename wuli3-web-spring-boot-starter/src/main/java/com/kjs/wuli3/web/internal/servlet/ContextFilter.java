package com.kjs.wuli3.web.internal.servlet;

import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.context.ClientIpResolver;
import com.kjs.wuli3.web.context.RequestIdResolver;
import com.kjs.wuli3.web.context.RequestIds;
import com.kjs.wuli3.web.context.WebContextProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 在 Servlet 请求期间建立并清理 Wuli3 调用上下文。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ContextFilter extends OncePerRequestFilter {

    private final ContextWriter contextWriter;
    private final AuthContextResolver authContextResolver;
    private final RequestIdResolver requestIdResolver;
    private final ClientIpResolver clientIpResolver;
    private final WebContextProperties contextProperties;

    /** 创建使用指定上下文解析器的过滤器。 */
    public ContextFilter(
            final ContextWriter contextWriter,
            final AuthContextResolver authContextResolver,
            final RequestIdResolver requestIdResolver,
            final ClientIpResolver clientIpResolver,
            final WebContextProperties contextProperties) {
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
        this.authContextResolver = Objects.requireNonNull(authContextResolver, "authContextResolver");
        this.requestIdResolver = Objects.requireNonNull(requestIdResolver, "requestIdResolver");
        this.clientIpResolver = Objects.requireNonNull(clientIpResolver, "clientIpResolver");
        this.contextProperties = Objects.requireNonNull(contextProperties, "contextProperties");
    }

    /**
     * 写入请求上下文、认证上下文和 MDC，并在请求结束后清理所有线程状态。
     *
     * <p>注意：无论过滤器链是否抛出异常，线程上下文都会在 finally 中清理。
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {
        final String requestId = this.requestIdResolver.resolve(request);
        final InvocationContext invocationContext =
                new InvocationContext(this.clientIpResolver.resolve(request), requestId);
        this.contextWriter.put(invocationContext);
        response.setHeader(this.contextProperties.getRequestIdHeaderName(), requestId);
        MDC.put(RequestIds.MDC_KEY, requestId);
        try {
            this.authContextResolver.resolve(request).ifPresent(this.contextWriter::put);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(RequestIds.MDC_KEY);
            this.contextWriter.clear();
        }
    }
}

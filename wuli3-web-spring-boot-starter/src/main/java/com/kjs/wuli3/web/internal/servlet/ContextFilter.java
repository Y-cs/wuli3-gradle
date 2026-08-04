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

public final class ContextFilter extends OncePerRequestFilter {

    private final ContextWriter contextWriter;
    private final AuthContextResolver authContextResolver;
    private final RequestIdResolver requestIdResolver;
    private final ClientIpResolver clientIpResolver;
    private final WebContextProperties contextProperties;

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

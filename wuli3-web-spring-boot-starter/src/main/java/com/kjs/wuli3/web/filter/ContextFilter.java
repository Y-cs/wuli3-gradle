package com.kjs.wuli3.web.filter;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.holder.ContextWriter;
import com.kjs.wuli3.web.context.WebContext;
import com.kjs.wuli3.web.wrapper.HttpServletCacheRequestWrapper;
import com.kjs.wuli3.web.wrapper.RequestId;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public final class ContextFilter extends OncePerRequestFilter {

    private final ContextWriter contextWriter;
    private final AuthContextResolver authContextResolver;

    public ContextFilter(ContextWriter contextWriter, AuthContextResolver authContextResolver) {
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
        this.authContextResolver = Objects.requireNonNull(authContextResolver, "authContextResolver");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // cache request
        final HttpServletCacheRequestWrapper wrappedRequest = new HttpServletCacheRequestWrapper(request);
        final String requestId = idOrGenerated(wrappedRequest.getHeader(RequestId.HEADER_NAME));
        // create web context
        final WebContext webContext = WebContext.from(wrappedRequest, requestId);
        contextWriter.put(webContext);

        // create invocation context
        final InvocationContext invocationContext = new InvocationContext(originIp(wrappedRequest), requestId);
        contextWriter.put(invocationContext);

        // create auth context
        final AuthContext authContext = authContextResolver.resolve(wrappedRequest);
        contextWriter.put(authContext);

        response.setHeader(RequestId.HEADER_NAME, requestId);
        MDC.put(RequestId.MDC_KEY, requestId);
        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            MDC.remove(RequestId.MDC_KEY);
            contextWriter.clear();
        }
    }

    private static String idOrGenerated(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return UUID.randomUUID()
                    .toString();
        }
        return value;
    }

    private static String originIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            return comma < 0 ? forwardedFor.trim() : forwardedFor.substring(0, comma)
                    .trim();
        }
        return request.getRemoteAddr();
    }
}

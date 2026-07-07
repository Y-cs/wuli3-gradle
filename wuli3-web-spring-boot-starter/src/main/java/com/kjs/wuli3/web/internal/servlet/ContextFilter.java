package com.kjs.wuli3.web.internal.servlet;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.config.properties.WebContextProperties;
import com.kjs.wuli3.web.context.ClientIpResolver;
import com.kjs.wuli3.web.context.RequestIdResolver;
import com.kjs.wuli3.web.context.RequestIds;
import com.kjs.wuli3.web.context.WebContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ContextFilter extends OncePerRequestFilter {

    private final ContextWriter contextWriter;
    private final AuthContextResolver authContextResolver;
    private final RequestIdResolver requestIdResolver;
    private final ClientIpResolver clientIpResolver;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final WebContextProperties contextProperties;

    public ContextFilter(final ContextWriter contextWriter, final AuthContextResolver authContextResolver,
            final RequestIdResolver requestIdResolver, final ClientIpResolver clientIpResolver,
            final HandlerExceptionResolver handlerExceptionResolver,
            final WebContextProperties contextProperties) {
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
        this.authContextResolver = Objects.requireNonNull(authContextResolver, "authContextResolver");
        this.requestIdResolver = Objects.requireNonNull(requestIdResolver, "requestIdResolver");
        this.clientIpResolver = Objects.requireNonNull(clientIpResolver, "clientIpResolver");
        this.handlerExceptionResolver = Objects.requireNonNull(handlerExceptionResolver, "handlerExceptionResolver");
        this.contextProperties = Objects.requireNonNull(contextProperties, "contextProperties");
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final String requestId = this.requestIdResolver.resolve(request);
        final InvocationContext invocationContext = new InvocationContext(this.clientIpResolver.resolve(request), requestId);
        this.contextWriter.put(invocationContext);
        response.setHeader(this.contextProperties.getRequestIdHeaderName(), requestId);
        MDC.put(RequestIds.MDC_KEY, requestId);
        try {
            final HttpServletRequest currentRequest;
            try {
                currentRequest = this.request(request);
            } catch (ErrorCodeException ex) {
                this.handleErrorCodeException(request, response, ex);
                return;
            }
            final WebContext webContext = WebContext.from(currentRequest, requestId);
            this.contextWriter.put(webContext);

            final AuthContext authContext = this.authContextResolver.resolve(currentRequest);
            this.contextWriter.put(authContext);

            filterChain.doFilter(currentRequest, response);
        } finally {
            MDC.remove(RequestIds.MDC_KEY);
            this.contextWriter.clear();
        }
    }

    private void handleErrorCodeException(final HttpServletRequest request, final HttpServletResponse response,
            final ErrorCodeException ex) {
        final ModelAndView modelAndView = this.handlerExceptionResolver.resolveException(request, response, null, ex);
        if (modelAndView == null) {
            throw ex;
        }
    }

    private HttpServletRequest request(final HttpServletRequest request) throws IOException {
        if (!this.shouldCacheBody(request)) {
            return request;
        }
        return new HttpServletCacheRequestWrapper(request, this.contextProperties.getMaxCacheBodySize()
                .toBytes());
    }

    private boolean shouldCacheBody(final HttpServletRequest request) {
        if (!this.contextProperties.isRequestBodyCacheEnabled()) {
            return false;
        }
        final String contentType = ContextFilter.normalizedContentType(request.getContentType());
        if (contentType == null) {
            return false;
        }
        if (ContextFilter.matchesAny(contentType, this.contextProperties.getExcludedBodyCacheContentTypes())) {
            return false;
        }
        return ContextFilter.matchesAny(contentType, this.contextProperties.getCacheableContentTypes());
    }

    private static boolean matchesAny(final String contentType, final List<String> patterns) {
        for (final String pattern : patterns) {
            if (ContextFilter.matches(contentType, pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(final String contentType, final String pattern) {
        final String normalizedPattern = ContextFilter.normalizedContentType(pattern);
        if (normalizedPattern == null) {
            return false;
        }
        if (normalizedPattern.endsWith("/*")) {
            return contentType.startsWith(normalizedPattern.substring(0, normalizedPattern.length() - 1));
        }
        final int suffixWildcard = normalizedPattern.indexOf("/*+");
        if (suffixWildcard > 0) {
            final String type = normalizedPattern.substring(0, suffixWildcard + 1);
            final String suffix = normalizedPattern.substring(suffixWildcard + 2);
            return contentType.startsWith(type) && contentType.endsWith(suffix);
        }
        return contentType.equals(normalizedPattern);
    }

    private static @Nullable String normalizedContentType(final @Nullable String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        final int semicolon = contentType.indexOf(';');
        final String value = semicolon < 0 ? contentType : contentType.substring(0, semicolon);
        return value.trim()
                .toLowerCase(Locale.ROOT);
    }
}

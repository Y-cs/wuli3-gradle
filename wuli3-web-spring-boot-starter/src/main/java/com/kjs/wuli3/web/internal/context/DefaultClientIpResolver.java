package com.kjs.wuli3.web.internal.context;

import com.kjs.wuli3.web.config.properties.WebContextProperties;
import com.kjs.wuli3.web.context.ClientIpResolver;
import org.jspecify.annotations.Nullable;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Default client IP resolver that only trusts forwarding headers when explicitly enabled.
 */
public final class DefaultClientIpResolver implements ClientIpResolver {

    private static final String FORWARDED = "Forwarded";

    private final WebContextProperties properties;

    public DefaultClientIpResolver(final WebContextProperties properties) {
        this.properties = properties;
    }

    @Override
    public String resolve(final HttpServletRequest request) {
        if (this.properties.isTrustedProxyEnabled()) {
            for (final String headerName : this.properties.getClientIpHeaderPriority()) {
                final String candidate = this.candidate(request, headerName);
                if (candidate != null && !candidate.isBlank()) {
                    return candidate;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private @Nullable String candidate(final HttpServletRequest request, final String headerName) {
        final String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return null;
        }
        if (FORWARDED.equalsIgnoreCase(headerName)) {
            return DefaultClientIpResolver.forwardedFor(value);
        }
        final int comma = value.indexOf(',');
        return comma < 0 ? value.trim() : value.substring(0, comma)
                .trim();
    }

    private static @Nullable String forwardedFor(final String value) {
        int start = 0;
        while (start <= value.length()) {
            final int semicolon = value.indexOf(';', start);
            final String part = semicolon < 0 ? value.substring(start) : value.substring(start, semicolon);
            final String trimmed = part.trim();
            final int equals = trimmed.indexOf('=');
            if (equals > 0 && "for".equalsIgnoreCase(trimmed.substring(0, equals)
                    .trim())) {
                return DefaultClientIpResolver.unquote(trimmed.substring(equals + 1)
                        .trim());
            }
            if (semicolon < 0) {
                return null;
            }
            start = semicolon + 1;
        }
        return null;
    }

    private static String unquote(final String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}

package com.kjs.wuli3.web.internal.context;

import com.kjs.wuli3.web.context.RequestIdResolver;
import com.kjs.wuli3.web.context.WebContextProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Default request id resolver with bounded external input.
 */
public final class DefaultRequestIdResolver implements RequestIdResolver {

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("[A-Za-z0-9._:-]+");

    private final WebContextProperties properties;

    public DefaultRequestIdResolver(final WebContextProperties properties) {
        this.properties = properties;
    }

    @Override
    public String resolve(final HttpServletRequest request) {
        final String externalRequestId = this.externalRequestId(request);
        if (!this.properties.isAcceptExternalRequestId() || externalRequestId == null || externalRequestId.isBlank()) {
            return DefaultRequestIdResolver.generate();
        }
        final String requestId = externalRequestId.trim();
        if (this.valid(requestId)) {
            return requestId;
        }
        return DefaultRequestIdResolver.generate();
    }

    private @Nullable String externalRequestId(final HttpServletRequest request) {
        return request.getHeader(this.properties.getRequestIdHeaderName());
    }

    private boolean valid(final String requestId) {
        return requestId.length() <= this.properties.getRequestIdMaxLength()
                && REQUEST_ID_PATTERN.matcher(requestId).matches();
    }

    private static String generate() {
        return UUID.randomUUID().toString();
    }
}

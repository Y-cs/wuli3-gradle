package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.context.AbstractContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.context.ContextKey;
import com.kjs.wuli3.propagation.context.LocalContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable local snapshot of the current HTTP request.
 */
@Getter
@ToString
public final class WebContext extends AbstractContext implements LocalContext {

    private final String requestId;
    private final Locale locale;
    private final String requestUri;
    private final String method;

    private WebContext(
            final Map<ContextKey<?>, Object> extensions,
            final String requestId,
            final Locale locale,
            final String requestUri,
            final String method) {
        super(extensions);
        this.requestId = requestId;
        this.locale = locale;
        this.requestUri = requestUri;
        this.method = method;
    }

    public static WebContext from(final HttpServletRequest request, final String requestId) {
        return new WebContext(Map.of(), requestId, request.getLocale(), request.getRequestURI(), request.getMethod());
    }

    @Override
    public Context snapshotCopy() {
        return new WebContext(this.extensionSnapshot(), this.requestId, this.locale, this.requestUri, this.method);
    }

    @Override
    public Class<? extends Context> type() {
        return WebContext.class;
    }
}

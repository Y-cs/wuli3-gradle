package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.store.ContextReader;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Accessor for the current web request context.
 */
@RequiredArgsConstructor
public class WebContextAccessor {

    private final ContextReader holder;

    public Optional<WebContext> current() {
        return this.holder.get(WebContext.class);
    }

    public Optional<String> requestId() {
        return this.current().map(WebContext::getRequestId);
    }

    public Optional<Locale> locale() {
        return this.current().map(WebContext::getLocale);
    }

    public Optional<String> requestUri() {
        return this.current().map(WebContext::getRequestUri);
    }

    public Optional<String> method() {
        return this.current().map(WebContext::getMethod);
    }
}

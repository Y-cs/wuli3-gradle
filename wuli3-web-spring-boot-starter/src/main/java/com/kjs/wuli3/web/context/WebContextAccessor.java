package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.store.ContextReader;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

    public Optional<Map<String, List<String>>> headers() {
        return this.current().map(WebContext::getHeaders);
    }

    public Optional<String> header(final String name) {
        return this.current().flatMap(context -> context.header(name));
    }

    public Optional<List<String>> headers(final String name) {
        return this.current().flatMap(context -> context.headers(name));
    }

    public Optional<Map<String, List<String>>> parameters() {
        return this.current().map(WebContext::getParameters);
    }

    public Optional<String> parameter(final String name) {
        return this.current().flatMap(context -> context.parameter(name));
    }

    public Optional<List<String>> parameters(final String name) {
        return this.current().flatMap(context -> context.parameters(name));
    }

    public Optional<Locale> locale() {
        return this.current().map(WebContext::getLocale);
    }

    public Optional<String> remoteAddr() {
        return this.current().map(WebContext::getRemoteAddr);
    }

    public Optional<String> requestUri() {
        return this.current().map(WebContext::getRequestUri);
    }

    public Optional<String> requestUrl() {
        return this.current().map(WebContext::getRequestUrl);
    }

    public Optional<String> method() {
        return this.current().map(WebContext::getMethod);
    }

    public Optional<String> queryString() {
        return this.current().map(WebContext::getQueryString);
    }
}

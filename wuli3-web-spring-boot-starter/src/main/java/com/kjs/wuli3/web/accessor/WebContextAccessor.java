package com.kjs.wuli3.web.accessor;

import com.kjs.wuli3.propagation.holder.ContextReader;
import com.kjs.wuli3.web.context.WebContext;
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
        return holder.get(WebContext.class);
    }

    public Optional<String> requestId() {
        return current().map(WebContext::getRequestId);
    }

    public Optional<Map<String, List<String>>> headers() {
        return current().map(WebContext::getHeaders);
    }

    public Optional<String> header(String name) {
        return current().flatMap(context -> context.header(name));
    }

    public Optional<List<String>> headers(String name) {
        return current().flatMap(context -> context.headers(name));
    }

    public Optional<Map<String, List<String>>> parameters() {
        return current().map(WebContext::getParameters);
    }

    public Optional<String> parameter(String name) {
        return current().flatMap(context -> context.parameter(name));
    }

    public Optional<List<String>> parameters(String name) {
        return current().flatMap(context -> context.parameters(name));
    }

    public Optional<Locale> locale() {
        return current().map(WebContext::getLocale);
    }

    public Optional<String> remoteAddr() {
        return current().map(WebContext::getRemoteAddr);
    }

    public Optional<String> requestUri() {
        return current().map(WebContext::getRequestUri);
    }

    public Optional<String> requestUrl() {
        return current().map(WebContext::getRequestUrl);
    }

    public Optional<String> method() {
        return current().map(WebContext::getMethod);
    }

    public Optional<String> queryString() {
        return current().map(WebContext::getQueryString);
    }
}

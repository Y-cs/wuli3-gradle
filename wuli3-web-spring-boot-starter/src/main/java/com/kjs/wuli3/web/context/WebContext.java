package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.context.AbstractContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.context.ContextKey;
import com.kjs.wuli3.propagation.context.LocalContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * Immutable local snapshot of the current HTTP request.
 */
@Getter
@ToString
public final class WebContext extends AbstractContext implements LocalContext {

    private final String requestId;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> parameters;
    private final Locale locale;
    private final String remoteAddr;
    private final String requestUri;
    private final String requestUrl;
    private final String method;
    private final @Nullable String queryString;

    private WebContext(
            final Map<ContextKey<?>, Object> extensions,
            final String requestId,
            final Map<String, List<String>> headers,
            final Map<String, List<String>> parameters,
            final Locale locale,
            final String remoteAddr,
            final String requestUri,
            final String requestUrl,
            final String method,
            final @Nullable String queryString) {
        super(extensions);
        this.requestId = requestId;
        this.headers = WebContext.immutableValues(headers);
        this.parameters = WebContext.immutableValues(parameters);
        this.locale = locale;
        this.remoteAddr = remoteAddr;
        this.requestUri = requestUri;
        this.requestUrl = requestUrl;
        this.method = method;
        this.queryString = queryString;
    }

    public static WebContext from(final HttpServletRequest request, final String requestId) {
        return new WebContext(
                Map.of(),
                requestId,
                WebContext.headers(request),
                WebContext.parameters(request),
                request.getLocale(),
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getRequestURL().toString(),
                request.getMethod(),
                request.getQueryString());
    }

    @Override
    public Context snapshotCopy() {
        return new WebContext(
                this.extensionSnapshot(),
                this.requestId,
                this.headers,
                this.parameters,
                this.locale,
                this.remoteAddr,
                this.requestUri,
                this.requestUrl,
                this.method,
                this.queryString);
    }

    public Optional<String> header(final String name) {
        return this.headers.getOrDefault(name, List.of()).stream().findFirst();
    }

    public Optional<List<String>> headers(final String name) {
        final List<String> values = this.headers.get(name);
        return values == null ? Optional.empty() : Optional.of(values);
    }

    public Optional<String> parameter(final String name) {
        return this.parameters.getOrDefault(name, List.of()).stream().findFirst();
    }

    public Optional<List<String>> parameters(final String name) {
        final List<String> values = this.parameters.get(name);
        return values == null ? Optional.empty() : Optional.of(values);
    }

    @Override
    public Class<? extends Context> type() {
        return WebContext.class;
    }

    private static Map<String, List<String>> headers(final HttpServletRequest request) {
        final Map<String, List<String>> values = new LinkedHashMap<>();
        final var names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            final String name = names.nextElement();
            values.put(name, Collections.unmodifiableList(new ArrayList<>(Collections.list(request.getHeaders(name)))));
        }
        return values;
    }

    private static Map<String, List<String>> parameters(final HttpServletRequest request) {
        final Map<String, List<String>> values = new LinkedHashMap<>();
        request.getParameterMap().forEach((name, rawValues) -> values.put(name, WebContext.listOf(rawValues)));
        return values;
    }

    private static List<String> listOf(final String[] rawValues) {
        final List<String> values = new ArrayList<>(rawValues.length);
        Collections.addAll(values, rawValues);
        return Collections.unmodifiableList(values);
    }

    private static Map<String, List<String>> immutableValues(final Map<String, List<String>> source) {
        final Map<String, List<String>> values = new LinkedHashMap<>();
        source.forEach((name, rawValues) -> values.put(name, List.copyOf(rawValues)));
        return Map.copyOf(values);
    }
}

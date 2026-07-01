package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.context.AbstractContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.context.LocalContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 当前请求的本地上下文快照。
 *
 * @author GuoYang create on 2026/6/25 19:06
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
            String requestId,
            Map<String, List<String>> headers,
            Map<String, List<String>> parameters,
            Locale locale,
            String remoteAddr,
            String requestUri,
            String requestUrl,
            String method,
            @Nullable String queryString
    ) {
        this.requestId = requestId;
        this.headers = Map.copyOf(headers);
        this.parameters = Map.copyOf(parameters);
        this.locale = locale;
        this.remoteAddr = remoteAddr;
        this.requestUri = requestUri;
        this.requestUrl = requestUrl;
        this.method = method;
        this.queryString = queryString;
    }

    public static WebContext from(HttpServletRequest request, String requestId) {
        return new WebContext(
                requestId,
                headers(request),
                parameters(request),
                request.getLocale(),
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getRequestURL().toString(),
                request.getMethod(),
                request.getQueryString()
        );
    }

    public Optional<String> header(String name) {
        return headers.getOrDefault(name, List.of())
                .stream()
                .findFirst();
    }

    public Optional<List<String>> headers(String name) {
        List<String> values = headers.get(name);
        return values == null ? Optional.empty() : Optional.of(values);
    }

    public Optional<String> parameter(String name) {
        return parameters.getOrDefault(name, List.of())
                .stream()
                .findFirst();
    }

    public Optional<List<String>> parameters(String name) {
        List<String> values = parameters.get(name);
        return values == null ? Optional.empty() : Optional.of(values);
    }

    @Override
    public Class<? extends Context> type() {
        return WebContext.class;
    }

    private static Map<String, List<String>> headers(HttpServletRequest request) {
        Map<String, List<String>> values = new LinkedHashMap<>();
        var names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            values.put(name, Collections.unmodifiableList(new ArrayList<>(Collections.list(request.getHeaders(name)))));
        }
        return values;
    }

    private static Map<String, List<String>> parameters(HttpServletRequest request) {
        Map<String, List<String>> values = new LinkedHashMap<>();
        request.getParameterMap()
                .forEach((name, rawValues) -> values.put(name, listOf(rawValues)));
        return values;
    }

    private static List<String> listOf(String[] rawValues) {
        List<String> values = new ArrayList<>(rawValues.length);
        Collections.addAll(values, rawValues);
        return Collections.unmodifiableList(values);
    }
}

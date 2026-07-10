package com.kjs.wuli3.web.json;

import com.kjs.wuli3.json.datatype.resource.ResourcePathResolver;
import com.kjs.wuli3.web.config.properties.WebJsonResourcePathProperties;
import org.jspecify.annotations.NullMarked;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * WebResourcePathResolver
 *
 * @author GuoYang create on 2026/7/9 17:08
 */
@NullMarked
public class WebResourcePathResolver implements ResourcePathResolver {

    private final Map<String, String> pathTypeMap;

    public WebResourcePathResolver(final WebJsonResourcePathProperties properties) {
        this.pathTypeMap = Optional.ofNullable(Objects.requireNonNull(properties, "properties")
                        .getPath())
                .map(Map::copyOf)
                .orElse(Map.of());
    }

    @Override
    public boolean supports(final String type) {
        return this.pathTypeMap.containsKey(type);
    }

    @Override
    public String serialize(final String type, final String path) {
        final String domain = this.pathTypeMap.get(type);
        if (domain == null || domain.isBlank() || path.isBlank() || WebResourcePathResolver.isAbsoluteUri(path)) {
            return path;
        }
        return WebResourcePathResolver.trimTrailingSlashes(domain)
                + "/"
                + WebResourcePathResolver.trimLeadingSlashes(path);
    }

    @Override
    public String deserialize(final String type, final String url) {
        final String domain = this.pathTypeMap.get(type);
        if (domain == null || domain.isBlank() || url.isBlank()) {
            return url;
        }
        final String normalizedDomain = WebResourcePathResolver.trimTrailingSlashes(domain);
        final String urlPrefix = normalizedDomain + "/";
        if (!url.startsWith(urlPrefix)) {
            return url;
        }
        return url.substring(normalizedDomain.length());
    }

    private static String trimTrailingSlashes(final String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private static String trimLeadingSlashes(final String value) {
        int start = 0;
        while (start < value.length() && value.charAt(start) == '/') {
            start++;
        }
        return value.substring(start);
    }

    private static boolean isAbsoluteUri(final String value) {
        if (value.startsWith("//")) {
            return true;
        }
        try {
            return URI.create(value)
                    .isAbsolute();
        } catch (final IllegalArgumentException ignored) {
            return false;
        }
    }
}

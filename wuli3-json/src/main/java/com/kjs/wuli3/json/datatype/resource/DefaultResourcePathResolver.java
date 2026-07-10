package com.kjs.wuli3.json.datatype.resource;

/**
 * Default resource resolver that preserves values unchanged.
 */
public final class DefaultResourcePathResolver implements ResourcePathResolver {
    public static final String DEFAULT_TYPE = "default";

    @Override
    public boolean supports(final String type) {
        return DefaultResourcePathResolver.DEFAULT_TYPE.equals(type);
    }

    @Override
    public String serialize(final String type, final String path) {
        return path;
    }

    @Override
    public String deserialize(final String type, final String url) {
        return url;
    }
}

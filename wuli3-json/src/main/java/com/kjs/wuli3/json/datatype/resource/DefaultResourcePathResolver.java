package com.kjs.wuli3.json.datatype.resource;

/**
 * 保持资源值不变的默认资源解析器。
 *
 * @author GuoYang create on 2026/8/17 11:53
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

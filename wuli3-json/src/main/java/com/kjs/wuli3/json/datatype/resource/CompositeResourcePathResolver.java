package com.kjs.wuli3.json.datatype.resource;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

/** 按声明顺序组合多个资源路径解析器，供同一个 JSON 映射器统一选择解析策略。
 *
 * <p>同一资源类型由列表中第一个支持它的解析器处理；没有解析器支持时保留原值。
 * 解析器列表通常来自 Spring {@code @Order} 排序结果。
 *
 * @author GuoYang create on 2026/9/4 15:00
 */
@NullMarked
public final class CompositeResourcePathResolver implements ResourcePathResolver {

    private final List<ResourcePathResolver> resolvers;

    public CompositeResourcePathResolver(final List<? extends ResourcePathResolver> resolvers) {
        Objects.requireNonNull(resolvers, "resolvers");
        this.resolvers = List.copyOf(resolvers);
    }

    /** 判断是否至少有一个解析器支持指定资源类型。 */
    @Override
    public boolean supports(final String type) {
        return this.resolvers.stream().anyMatch(resolver -> resolver.supports(type));
    }

    /** 使用第一个支持指定类型的解析器转换存储路径。 */
    @Override
    public String serialize(final String type, final String path) {
        return this.findResolver(type)
                .map(resolver -> resolver.serialize(type, path))
                .orElse(path);
    }

    /** 使用第一个支持指定类型的解析器还原公开 URL。 */
    @Override
    public String deserialize(final String type, final String url) {
        return this.findResolver(type)
                .map(resolver -> resolver.deserialize(type, url))
                .orElse(url);
    }

    private java.util.Optional<ResourcePathResolver> findResolver(final String type) {
        return this.resolvers.stream()
                .filter(resolver -> resolver.supports(type))
                .findFirst();
    }
}

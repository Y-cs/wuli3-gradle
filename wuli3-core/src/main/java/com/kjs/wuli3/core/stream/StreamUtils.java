package com.kjs.wuli3.core.stream;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 提供 Stream 过滤和按映射键去重的工具方法。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@NullMarked
public final class StreamUtils {

    private static final Object NULL_KEY = new Object();

    private StreamUtils() {}

    /**
     * 过滤流中的空值。
     *
     * @param stream 待过滤的流
     * @param <T> 元素类型
     * @return 仅包含非空元素的流
     */
    public static <T> Stream<T> filterNotNull(final Stream<? extends @Nullable T> stream) {
        Objects.requireNonNull(stream, "stream");
        return stream.flatMap((final T value) -> value == null ? Stream.empty() : Stream.of(value));
    }

    /**
     * 创建按映射键保留一个元素的有状态谓词。
     *
     * <p>顺序流保留最先遇到的元素；并行流对每个键保留任意元素，不能依赖遇到顺序。一个谓词实例只应服务于一次流遍历，不能在不同操作间复用。
     *
     * @param keyMapper 元素到去重键的映射函数
     * @param <T> 元素类型
     * @param <K> 去重键类型
     * @return 按映射键去重的有状态谓词
     *
     * 注意：同一个谓词只能用于一次流遍历；并行流不保证保留遇到顺序。
     */
    public static <T, K> Predicate<T> distinctBy(final Function<? super T, ? extends @Nullable K> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper");
        final Set<Object> seen = ConcurrentHashMap.newKeySet();
        return (final T value) -> {
            final K key = keyMapper.apply(value);
            return seen.add(key == null ? StreamUtils.NULL_KEY : key);
        };
    }
}

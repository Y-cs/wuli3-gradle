package com.kjs.wuli3.core.stream;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/**
 * 提供保留插入顺序的 Map 收集器。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@NullMarked
public final class MoreCollectors {

    private MoreCollectors() {}

    /**
     * 收集为保留插入顺序的 Map；出现重复键时抛出异常。
     *
     * @param keyMapper 键映射函数
     * @param valueMapper 值映射函数
     * @param <T> 输入元素类型
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 保留插入顺序的 Map 收集器
     */
    public static <T, K, V> Collector<T, ?, Map<K, V>> toLinkedMap(
            final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        return MoreCollectors.toLinkedMap(keyMapper, valueMapper, MapMerger::throwDuplicate);
    }

    /**
     * 收集为保留插入顺序的 Map，并使用指定函数合并重复键。
     *
     * @param keyMapper 键映射函数
     * @param valueMapper 值映射函数
     * @param mergeFunction 重复键的合并函数
     * @param <T> 输入元素类型
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 保留插入顺序的 Map 收集器
     */
    public static <T, K, V> Collector<T, ?, Map<K, V>> toLinkedMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends V> valueMapper,
            final BinaryOperator<V> mergeFunction) {
        return Collectors.toMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new);
    }
}

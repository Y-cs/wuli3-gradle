package com.kjs.wuli3.core.stream;

import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@NullMarked
public final class MoreCollectors {

    private MoreCollectors() {
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toLinkedMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends V> valueMapper) {
        return MoreCollectors.toLinkedMap(keyMapper, valueMapper, MapMerger::throwDuplicate);
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toLinkedMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends V> valueMapper,
            final BinaryOperator<V> mergeFunction) {
        return Collectors.toMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new);
    }
}

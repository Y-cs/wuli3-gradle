package com.kjs.wuli3.core.stream;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class StreamUtils {

    private static final Object NULL_KEY = new Object();

    private StreamUtils() {}

    public static <T> Stream<T> filterNotNull(final Stream<? extends @Nullable T> stream) {
        Objects.requireNonNull(stream, "stream");
        return stream.flatMap((final T value) -> value == null ? Stream.empty() : Stream.of(value));
    }

    public static <T, K> Predicate<T> distinctBy(final Function<? super T, ? extends @Nullable K> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper");
        final Set<Object> seen = ConcurrentHashMap.newKeySet();
        return (final T value) -> {
            final K key = keyMapper.apply(value);
            return seen.add(key == null ? StreamUtils.NULL_KEY : key);
        };
    }
}

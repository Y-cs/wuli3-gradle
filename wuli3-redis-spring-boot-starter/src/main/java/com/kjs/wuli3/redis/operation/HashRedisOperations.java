package com.kjs.wuli3.redis.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.redis.RedisKey;
import com.kjs.wuli3.redis.codec.JsonRedisCodec;
import com.kjs.wuli3.redis.codec.RedisCodec;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 基于字符串 RedisTemplate 执行 Hash JSON 操作。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class HashRedisOperations {

    private final StringRedisTemplate redisTemplate;
    private final HashOperations<String, String, String> hashOperations;
    private final RedisCodec codec;

    /** 使用标准 JSON Codec 创建 Hash 操作入口。 */
    public HashRedisOperations(final StringRedisTemplate redisTemplate) {
        this(redisTemplate, JsonRedisCodec.INSTANCE);
    }

    /** 使用指定 Codec 创建 Hash 操作入口。 */
    public HashRedisOperations(final StringRedisTemplate redisTemplate, final RedisCodec codec) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.hashOperations = this.redisTemplate.opsForHash();
    }

    /** 写入单个字段，并在需要时刷新整个 Hash 的过期时间。 */
    public void put(final RedisKey key, final String field, final Object value) {
        HashRedisOperations.validateKeyAndField(key, field);
        this.hashOperations.put(key.value(), field, this.codec.encode(Objects.requireNonNull(value, "value")));
        this.refreshAfterMutation(key, 1L);
    }

    /** 批量写入字段，并在需要时刷新整个 Hash 的过期时间。 */
    public void putAll(final RedisKey key, final Map<String, ?> values) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(values, "values");
        if (values.isEmpty()) {
            return;
        }
        final Map<String, String> encodedValues = new LinkedHashMap<>();
        values.forEach((field, value) -> {
            HashRedisOperations.validateField(field);
            encodedValues.put(field, this.codec.encode(Objects.requireNonNull(value, "value")));
        });
        this.hashOperations.putAll(key.value(), encodedValues);
        this.refreshAfterMutation(key, encodedValues.size());
    }

    /** 仅在字段不存在时写入值。 */
    public boolean putIfAbsent(final RedisKey key, final String field, final Object value) {
        HashRedisOperations.validateKeyAndField(key, field);
        final boolean added = this.hashOperations.putIfAbsent(
                key.value(), field, this.codec.encode(Objects.requireNonNull(value, "value")));
        this.refreshAfterMutation(key, added ? 1L : 0L);
        return added;
    }

    /** 按具体类型读取字段值。 */
    public <T> Optional<T> get(final RedisKey key, final String field, final Class<T> type) {
        HashRedisOperations.validateKeyAndField(key, field);
        Objects.requireNonNull(type, "type");
        final String encodedValue = this.hashOperations.get(key.value(), field);
        return encodedValue == null ? Optional.empty() : Optional.ofNullable(this.codec.decode(encodedValue, type));
    }

    /** 按泛型类型读取字段值。 */
    public <T> Optional<T> get(final RedisKey key, final String field, final TypeReference<T> typeReference) {
        HashRedisOperations.validateKeyAndField(key, field);
        Objects.requireNonNull(typeReference, "typeReference");
        final String encodedValue = this.hashOperations.get(key.value(), field);
        return encodedValue == null
                ? Optional.empty()
                : Optional.ofNullable(this.codec.decode(encodedValue, typeReference));
    }

    /** 按具体类型读取全部字段。 */
    public <T> Map<String, T> entries(final RedisKey key, final Class<T> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        final Map<String, T> decodedValues = new LinkedHashMap<>();
        this.hashOperations.entries(key.value()).forEach((field, encodedValue) -> {
            final T value = this.codec.decode(encodedValue, type);
            if (value != null) {
                decodedValues.put(field, value);
            }
        });
        return Map.copyOf(decodedValues);
    }

    /** 按泛型类型读取全部字段。 */
    public <T> Map<String, T> entries(final RedisKey key, final TypeReference<T> typeReference) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(typeReference, "typeReference");
        final Map<String, T> decodedValues = new LinkedHashMap<>();
        this.hashOperations.entries(key.value()).forEach((field, encodedValue) -> {
            final T value = this.codec.decode(encodedValue, typeReference);
            if (value != null) {
                decodedValues.put(field, value);
            }
        });
        return Map.copyOf(decodedValues);
    }

    /** 删除字段并返回实际删除数量。 */
    public long delete(final RedisKey key, final String... fields) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(fields, "fields");
        for (final String field : fields) {
            HashRedisOperations.validateField(field);
        }
        return this.hashOperations.delete(key.value(), (Object[]) fields);
    }

    /** 判断字段是否存在。 */
    public boolean hasField(final RedisKey key, final String field) {
        HashRedisOperations.validateKeyAndField(key, field);
        return this.hashOperations.hasKey(key.value(), field);
    }

    /** 返回字段数量。 */
    public long size(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        return this.hashOperations.size(key.value());
    }

    private void refreshExpiration(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        final Duration timeToLive =
                key.timeToLive().orElseThrow(() -> new IllegalArgumentException("永久 Redis key 没有可刷新的过期时间"));
        this.redisTemplate.expire(key.value(), timeToLive);
    }

    private void refreshAfterMutation(final RedisKey key, final long mutationCount) {
        if (mutationCount > 0L && key.timeToLive().isPresent()) {
            this.refreshExpiration(key);
        }
    }

    private static void validateKeyAndField(final RedisKey key, final String field) {
        Objects.requireNonNull(key, "key");
        HashRedisOperations.validateField(field);
    }

    private static void validateField(final String field) {
        Objects.requireNonNull(field, "field");
        if (field.isBlank()) {
            throw new IllegalArgumentException("Redis Hash field 不能为空");
        }
    }
}

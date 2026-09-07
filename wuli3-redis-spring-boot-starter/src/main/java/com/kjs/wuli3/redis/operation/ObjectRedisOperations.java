package com.kjs.wuli3.redis.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.redis.RedisKey;
import com.kjs.wuli3.redis.codec.JsonRedisCodec;
import com.kjs.wuli3.redis.codec.RedisCodec;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** 基于 Redis String 执行类型安全的结构化对象操作。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ObjectRedisOperations {

    private final ValueOperations<String, String> valueOperations;
    private final RedisCodec codec;

    /** 使用标准 JSON Codec 创建对象操作入口。 */
    public ObjectRedisOperations(final StringRedisTemplate redisTemplate) {
        this(redisTemplate, JsonRedisCodec.INSTANCE);
    }

    /** 使用指定 Codec 创建对象操作入口。 */
    public ObjectRedisOperations(final StringRedisTemplate redisTemplate, final RedisCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
        this.valueOperations =
                Objects.requireNonNull(redisTemplate, "redisTemplate").opsForValue();
    }

    /** 按 key 的过期策略写入对象。 */
    public void set(final RedisKey key, final Object value) {
        Objects.requireNonNull(key, "key");
        final String encodedValue = this.codec.encode(Objects.requireNonNull(value, "value"));
        final Optional<Duration> timeToLive = key.timeToLive();
        if (timeToLive.isPresent()) {
            this.valueOperations.set(key.value(), encodedValue, timeToLive.orElseThrow());
            return;
        }
        this.valueOperations.set(key.value(), encodedValue);
    }

    /** 仅在 key 不存在时原子写入对象及其过期时间。 */
    public boolean setIfAbsent(final RedisKey key, final Object value) {
        Objects.requireNonNull(key, "key");
        final String encodedValue = this.codec.encode(Objects.requireNonNull(value, "value"));
        final Optional<Duration> timeToLive = key.timeToLive();
        final Boolean stored = timeToLive.isPresent()
                ? this.valueOperations.setIfAbsent(key.value(), encodedValue, timeToLive.orElseThrow())
                : this.valueOperations.setIfAbsent(key.value(), encodedValue);
        return Boolean.TRUE.equals(stored);
    }

    /** 按具体类型读取对象。 */
    public <T> Optional<T> get(final RedisKey key, final Class<T> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        final String encodedValue = this.valueOperations.get(key.value());
        return encodedValue == null ? Optional.empty() : Optional.ofNullable(this.codec.decode(encodedValue, type));
    }

    /** 按泛型类型读取对象。 */
    public <T> Optional<T> get(final RedisKey key, final TypeReference<T> typeReference) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(typeReference, "typeReference");
        final String encodedValue = this.valueOperations.get(key.value());
        return encodedValue == null
                ? Optional.empty()
                : Optional.ofNullable(this.codec.decode(encodedValue, typeReference));
    }
}

package com.kjs.wuli3.redis.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.redis.RedisKey;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** 基于 Redis String 执行类型安全的 JSON 对象操作。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ObjectRedisOperations {

    private final ValueOperations<String, String> valueOperations;

    public ObjectRedisOperations(final StringRedisTemplate redisTemplate) {
        this.valueOperations =
                Objects.requireNonNull(redisTemplate, "redisTemplate").opsForValue();
    }

    /** 按 key 的过期策略写入对象。 */
    public void set(final RedisKey key, final Object value) {
        Objects.requireNonNull(key, "key");
        final String json = Jsons.toJson(Objects.requireNonNull(value, "value"));
        final Optional<Duration> timeToLive = key.timeToLive();
        if (timeToLive.isPresent()) {
            this.valueOperations.set(key.value(), json, timeToLive.orElseThrow());
            return;
        }
        this.valueOperations.set(key.value(), json);
    }

    /** 仅在 key 不存在时原子写入对象及其过期时间。 */
    public boolean setIfAbsent(final RedisKey key, final Object value) {
        Objects.requireNonNull(key, "key");
        final String json = Jsons.toJson(Objects.requireNonNull(value, "value"));
        final Optional<Duration> timeToLive = key.timeToLive();
        final Boolean stored = timeToLive.isPresent()
                ? this.valueOperations.setIfAbsent(key.value(), json, timeToLive.orElseThrow())
                : this.valueOperations.setIfAbsent(key.value(), json);
        return Boolean.TRUE.equals(stored);
    }

    /** 按具体类型读取对象。 */
    public <T> Optional<T> get(final RedisKey key, final Class<T> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        final String json = this.valueOperations.get(key.value());
        return json == null ? Optional.empty() : Optional.of(Jsons.fromJson(json, type));
    }

    /** 按泛型类型读取对象。 */
    public <T> Optional<T> get(final RedisKey key, final TypeReference<T> typeReference) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(typeReference, "typeReference");
        final String json = this.valueOperations.get(key.value());
        return json == null ? Optional.empty() : Optional.of(Jsons.fromJson(json, typeReference));
    }
}

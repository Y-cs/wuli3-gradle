package com.kjs.wuli3.redis.operation;

import com.kjs.wuli3.redis.RedisKey;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** 基于 StringRedisTemplate 执行 Redis String 操作。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class StringRedisOperations {

    private final StringRedisTemplate redisTemplate;
    private final ValueOperations<String, String> valueOperations;

    public StringRedisOperations(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.valueOperations = this.redisTemplate.opsForValue();
    }

    /** 按 key 的过期策略写入字符串。 */
    public void set(final RedisKey key, final String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        final Optional<Duration> timeToLive = key.timeToLive();
        if (timeToLive.isPresent()) {
            this.valueOperations.set(key.value(), value, timeToLive.orElseThrow());
            return;
        }
        this.valueOperations.set(key.value(), value);
    }

    /** 仅在 key 不存在时原子写入字符串及其过期时间。 */
    public boolean setIfAbsent(final RedisKey key, final String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        final Optional<Duration> timeToLive = key.timeToLive();
        final Boolean stored = timeToLive.isPresent()
                ? this.valueOperations.setIfAbsent(key.value(), value, timeToLive.orElseThrow())
                : this.valueOperations.setIfAbsent(key.value(), value);
        return Boolean.TRUE.equals(stored);
    }

    /** 读取字符串，key 不存在时返回空值。 */
    public Optional<String> get(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(this.valueOperations.get(key.value()));
    }

    /** 对字符串整数执行原子自增，并在需要时刷新过期时间。 */
    public long increment(final RedisKey key, final long delta) {
        Objects.requireNonNull(key, "key");
        final Long value = this.valueOperations.increment(key.value(), delta);
        if (key.timeToLive().isPresent()) {
            this.redisTemplate.expire(key.value(), key.timeToLive().orElseThrow());
        }
        if (value == null) {
            throw new IllegalStateException("Redis 自增操作未返回结果");
        }
        return value;
    }
}

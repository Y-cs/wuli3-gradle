package com.kjs.wuli3.redis;

import com.kjs.wuli3.redis.operation.HashRedisOperations;
import com.kjs.wuli3.redis.operation.ObjectRedisOperations;
import com.kjs.wuli3.redis.operation.SetRedisOperations;
import com.kjs.wuli3.redis.operation.StringRedisOperations;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 聚合不同 Redis 数据结构操作，并承载整 key 的通用操作。 */
public final class RedisSupport {

    private final StringRedisTemplate redisTemplate;
    private final StringRedisOperations stringOperations;
    private final ObjectRedisOperations objectOperations;
    private final HashRedisOperations hashOperations;
    private final SetRedisOperations setOperations;

    public RedisSupport(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.stringOperations = new StringRedisOperations(this.redisTemplate);
        this.objectOperations = new ObjectRedisOperations(this.redisTemplate);
        this.hashOperations = new HashRedisOperations(this.redisTemplate);
        this.setOperations = new SetRedisOperations(this.redisTemplate);
    }

    public StringRedisTemplate redisTemplate() {
        return this.redisTemplate;
    }

    /** 返回字符串操作入口。 */
    public StringRedisOperations stringOperations() {
        return this.stringOperations;
    }

    /** 返回普通 JSON 对象操作入口。 */
    public ObjectRedisOperations objectOperations() {
        return this.objectOperations;
    }

    /** 返回 Hash 操作入口。 */
    public HashRedisOperations hashOperations() {
        return this.hashOperations;
    }

    /** 返回 Set 操作入口。 */
    public SetRedisOperations setOperations() {
        return this.setOperations;
    }

    /** 删除完整 key，并返回是否实际删除。 */
    public boolean delete(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        return this.redisTemplate.delete(key.value());
    }

    /** 批量删除完整 key，并返回实际删除数量。 */
    public long delete(final Collection<RedisKey> keys) {
        Objects.requireNonNull(keys, "keys");
        final List<String> keyValues = keys.stream()
                .map(key -> Objects.requireNonNull(key, "key").value())
                .toList();
        return this.redisTemplate.delete(keyValues);
    }

    /** 判断完整 key 是否存在。 */
    public boolean exists(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        return this.redisTemplate.hasKey(key.value());
    }

    /** 使用 key 自带的 TTL 重新设置过期时间。 */
    public boolean expire(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        final Duration timeToLive =
                key.timeToLive().orElseThrow(() -> new IllegalArgumentException("永久 Redis key 没有可刷新的过期时间"));
        return Boolean.TRUE.equals(this.redisTemplate.expire(key.value(), timeToLive));
    }

    /** 为指定 key 设置新的过期时间。 */
    public boolean expire(final String key, final Duration timeToLive) {
        return this.expire(RedisKey.expiring(key, timeToLive));
    }
}

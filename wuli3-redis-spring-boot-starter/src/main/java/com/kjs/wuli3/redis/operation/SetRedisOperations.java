package com.kjs.wuli3.redis.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.redis.RedisKey;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 基于字符串 RedisTemplate 执行 Set JSON 操作。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class SetRedisOperations {

    private final StringRedisTemplate redisTemplate;
    private final SetOperations<String, String> setOperations;

    public SetRedisOperations(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.setOperations = this.redisTemplate.opsForSet();
    }

    /** 添加成员，并在实际新增成员后刷新 key 的过期时间。 */
    public long add(final RedisKey key, final Object... values) {
        Objects.requireNonNull(key, "key");
        final String[] encodedValues = SetRedisOperations.encodeValues(values);
        final Long added = this.setOperations.add(key.value(), encodedValues);
        final long addedCount = added == null ? 0L : added;
        this.refreshAfterMutation(key, addedCount);
        return addedCount;
    }

    /** 删除成员并返回实际删除数量。 */
    public long remove(final RedisKey key, final Object... values) {
        Objects.requireNonNull(key, "key");
        final String[] encodedValues = SetRedisOperations.encodeValues(values);
        final Long removed = this.setOperations.remove(key.value(), (Object[]) encodedValues);
        return removed == null ? 0L : removed;
    }

    /** 判断成员是否存在。 */
    public boolean contains(final RedisKey key, final Object value) {
        Objects.requireNonNull(key, "key");
        return Boolean.TRUE.equals(
                this.setOperations.isMember(key.value(), Jsons.toJson(Objects.requireNonNull(value, "value"))));
    }

    /** 按具体类型读取全部成员。 */
    public <T> Set<T> members(final RedisKey key, final Class<T> type) {
        Objects.requireNonNull(type, "type");
        return this.decodeMembers(key, json -> Jsons.fromJson(json, type));
    }

    /** 按泛型类型读取全部成员。 */
    public <T> Set<T> members(final RedisKey key, final TypeReference<T> typeReference) {
        Objects.requireNonNull(typeReference, "typeReference");
        return this.decodeMembers(key, json -> Jsons.fromJson(json, typeReference));
    }

    /** 返回成员数量。 */
    public long size(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        final Long size = this.setOperations.size(key.value());
        return size == null ? 0L : size;
    }

    private void refreshExpiration(final RedisKey key) {
        Objects.requireNonNull(key, "key");
        final Duration timeToLive =
                key.timeToLive().orElseThrow(() -> new IllegalArgumentException("永久 Redis key 没有可刷新的过期时间"));
        this.redisTemplate.expire(key.value(), timeToLive);
    }

    private <T> Set<T> decodeMembers(final RedisKey key, final Function<String, T> decoder) {
        Objects.requireNonNull(key, "key");
        final Set<String> encodedMembers = this.setOperations.members(key.value());
        if (encodedMembers == null || encodedMembers.isEmpty()) {
            return Set.of();
        }
        final Set<T> decodedMembers = new LinkedHashSet<>();
        encodedMembers.forEach(json -> decodedMembers.add(decoder.apply(json)));
        return Set.copyOf(decodedMembers);
    }

    private void refreshAfterMutation(final RedisKey key, final long mutationCount) {
        if (mutationCount > 0L && key.timeToLive().isPresent()) {
            this.refreshExpiration(key);
        }
    }

    private static String[] encodeValues(final Object[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            throw new IllegalArgumentException("Redis Set 操作至少需要一个成员");
        }
        final String[] encodedValues = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            encodedValues[index] = Jsons.toJson(Objects.requireNonNull(values[index], "value"));
        }
        return encodedValues;
    }
}

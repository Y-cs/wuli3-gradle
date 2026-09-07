package com.kjs.wuli3.redis.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.json.core.Jsons;
import org.jspecify.annotations.Nullable;

/**
 * 使用项目标准 {@link Jsons} 处理 Redis 结构化值的默认 Codec。
 *
 * @author GuoYang create on 2026/9/7 10:40
 */
public final class JsonRedisCodec implements RedisCodec {

    /** 无状态 JSON Codec 单例。 */
    public static final JsonRedisCodec INSTANCE = new JsonRedisCodec();

    private JsonRedisCodec() {}

    @Override
    public String encode(final Object value) {
        return Jsons.toJson(value);
    }

    @Override
    public <T> @Nullable T decode(final String encodedValue, final Class<T> type) {
        return Jsons.fromJson(encodedValue, type);
    }

    @Override
    public <T> @Nullable T decode(final String encodedValue, final TypeReference<T> typeReference) {
        return Jsons.fromJson(encodedValue, typeReference);
    }
}

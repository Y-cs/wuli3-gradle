package com.kjs.wuli3.redis.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jspecify.annotations.Nullable;

/**
 * Redis 结构化值的编码和解码约定。
 *
 * <p>该接口只作用于 {@code ObjectRedisOperations}、Hash value 和 Set member，不影响 Redis 原生字符串、
 * key、field 或其他控制命令。实现需要保证编码和解码格式相互兼容。
 *
 * @author GuoYang create on 2026/9/7 10:40
 */
public interface RedisCodec {

    /** 将结构化值编码为 Redis 字符串。 */
    String encode(Object value);

    /** 按具体类型解码 Redis 字符串；编码内容为顶层 {@code null} 时返回 {@code null}。 */
    <T> @Nullable T decode(String encodedValue, Class<T> type);

    /** 按泛型类型解码 Redis 字符串；编码内容为顶层 {@code null} 时返回 {@code null}。 */
    <T> @Nullable T decode(String encodedValue, TypeReference<T> typeReference);
}

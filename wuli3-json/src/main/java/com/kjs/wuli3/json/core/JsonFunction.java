package com.kjs.wuli3.json.core;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 可抛出 Jackson 底层受检异常的 JSON 操作。
 *
 * @param <T> 操作结果类型
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface JsonFunction<T> {

    /** 使用指定映射器执行 JSON 操作。 */
    T apply(ObjectMapper objectMapper) throws Exception;
}

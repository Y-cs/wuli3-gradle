package com.kjs.wuli3.json.core;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON operation that may throw checked exceptions from Jackson's low-level APIs.
 *
 * @param <T> operation result type
 */
@FunctionalInterface
public interface JsonFunction<T> {

    T apply(ObjectMapper objectMapper) throws Exception;
}

package com.kjs.wuli3.json.core;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JSON infrastructure errors.
 */
@Getter
@RequiredArgsConstructor
@ErrorModule("JSON")
public enum JsonErrors implements ErrorCode {
    SERIALIZATION_FAILED("JSON序列化失败"),
    DESERIALIZATION_FAILED("JSON反序列化失败"),
    ;

    private final String message;
}

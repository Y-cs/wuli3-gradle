package com.kjs.wuli3.json.core;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorMetadata;
import com.kjs.wuli3.core.error.model.ErrorModule;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JSON 基础设施错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(
        name = "JSON",
        defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.CRITICAL))
public enum JsonErrors implements ErrorCode {
    SERIALIZATION_FAILED("JSON序列化失败"),
    DESERIALIZATION_FAILED("JSON反序列化失败"),
    ;

    private final String message;
}

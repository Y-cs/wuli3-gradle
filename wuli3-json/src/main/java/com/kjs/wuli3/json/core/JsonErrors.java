package com.kjs.wuli3.json.core;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.metadata.ErrorModule;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
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
        value = "JSON",
        policy =
                @ErrorPolicy(
                        severity = ErrorSeverity.CRITICAL,
                        visibility = ErrorVisibility.INTERNAL,
                        origin = ErrorOrigin.SYSTEM))
public enum JsonErrors implements ErrorCode {
    SERIALIZATION_FAILED("JSON序列化失败"),
    DESERIALIZATION_FAILED("JSON反序列化失败"),
    ;

    private final String message;
}

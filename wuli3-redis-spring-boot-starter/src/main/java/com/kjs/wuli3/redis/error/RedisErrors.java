package com.kjs.wuli3.redis.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.metadata.ErrorModule;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Redis 基础设施错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(
        value = "REDIS",
        policy =
                @ErrorPolicy(
                        severity = ErrorSeverity.WARNING,
                        visibility = ErrorVisibility.INTERNAL,
                        origin = ErrorOrigin.SYSTEM))
public enum RedisErrors implements ErrorCode {
    ID_ALLOCATION_FAILED("Redis ID allocation failed"),
    ID_CLOCK_ROLLBACK("Redis ID generator clock moved backwards"),
    ID_SEQUENCE_EXHAUSTED("Redis ID sequence exhausted for current minute"),
    LOCK_ACQUISITION_FAILED("Redis lock acquisition failed"),
    LOCK_INTERRUPTED("Redis lock wait interrupted"),
    ;

    private final String message;
}

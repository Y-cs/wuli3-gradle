package com.kjs.wuli3.redis.error;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorMetadata;
import com.kjs.wuli3.core.error.model.ErrorModule;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Redis 基础设施错误码。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@RequiredArgsConstructor
@ErrorModule(name = "REDIS", defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.WARNING))
public enum RedisErrors implements ErrorCode {
    ID_ALLOCATION_FAILED("Redis ID allocation failed"),
    ID_CLOCK_ROLLBACK("Redis ID generator clock moved backwards"),
    ID_SEQUENCE_EXHAUSTED("Redis ID sequence exhausted for current minute"),
    LOCK_ACQUISITION_FAILED("Redis lock acquisition failed"),
    LOCK_INTERRUPTED("Redis lock wait interrupted");

    private final String message;
}

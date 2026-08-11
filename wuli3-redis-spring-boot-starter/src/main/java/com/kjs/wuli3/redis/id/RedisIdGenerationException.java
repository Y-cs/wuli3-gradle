package com.kjs.wuli3.redis.id;

import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.redis.error.RedisErrors;
import java.io.Serial;

/** Redis ID 分配失败时抛出的异常。 */
public final class RedisIdGenerationException extends ErrorCodeException {

    @Serial
    private static final long serialVersionUID = 1L;

    RedisIdGenerationException(final RedisErrors errorCode, final String message) {
        super(errorCode, message);
    }
}

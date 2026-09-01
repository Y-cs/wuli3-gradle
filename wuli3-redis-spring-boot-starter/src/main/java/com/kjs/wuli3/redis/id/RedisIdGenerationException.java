package com.kjs.wuli3.redis.id;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.redis.error.RedisErrors;
import java.io.Serial;

/** Redis ID 分配失败时抛出的异常。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class RedisIdGenerationException extends ErrorCodeException {

    @Serial
    private static final long serialVersionUID = 1L;

    RedisIdGenerationException(final RedisErrors errorCode, final String message) {
        super(errorCode, message);
    }
}

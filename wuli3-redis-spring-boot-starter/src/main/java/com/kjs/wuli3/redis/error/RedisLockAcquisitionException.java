package com.kjs.wuli3.redis.error;

import com.kjs.wuli3.core.error.ErrorCodeException;
import java.io.Serial;

/** 在等待时间内无法获取必需的 Redis 锁时抛出的异常。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class RedisLockAcquisitionException extends ErrorCodeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RedisLockAcquisitionException(final String key) {
        super(RedisErrors.LOCK_ACQUISITION_FAILED, "Failed to acquire Redis lock: " + key);
    }
}

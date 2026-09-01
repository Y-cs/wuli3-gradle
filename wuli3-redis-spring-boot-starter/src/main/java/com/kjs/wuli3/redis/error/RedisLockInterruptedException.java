package com.kjs.wuli3.redis.error;

import com.kjs.wuli3.core.error.ErrorCodeException;
import java.io.Serial;

/** 等待 Redis 锁的线程被中断并恢复中断标记后抛出的异常。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class RedisLockInterruptedException extends ErrorCodeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RedisLockInterruptedException(final String key, final InterruptedException cause) {
        super(RedisErrors.LOCK_INTERRUPTED, "Interrupted while waiting for Redis lock: " + key, cause);
    }
}

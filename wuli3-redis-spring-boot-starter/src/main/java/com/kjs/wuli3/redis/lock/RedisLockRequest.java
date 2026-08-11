package com.kjs.wuli3.redis.lock;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/** 一次分布式锁获取请求的不可变参数。 */
public record RedisLockRequest(String key, Duration waitTime, Optional<Duration> leaseTime) {

    private static final Duration MINIMUM_LEASE_TIME = Duration.ofMillis(1);

    public RedisLockRequest {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(waitTime, "waitTime");
        Objects.requireNonNull(leaseTime, "leaseTime");
        if (key.isBlank()) {
            throw new IllegalArgumentException("Redis lock key must not be blank");
        }
        RedisLockRequest.validateWaitTime(waitTime);
        leaseTime.ifPresent(RedisLockRequest::validateLeaseTime);
    }

    /** 使用 Redisson watchdog 在持有者存活期间自动续期。 */
    public static RedisLockRequest watchdog(final String key, final Duration waitTime) {
        return new RedisLockRequest(key, waitTime, Optional.empty());
    }

    /** 使用 Redisson 不会自动续期的固定租约。 */
    public static RedisLockRequest fixedLease(final String key, final Duration waitTime, final Duration leaseTime) {
        return new RedisLockRequest(key, waitTime, Optional.of(Objects.requireNonNull(leaseTime, "leaseTime")));
    }

    private static void validateWaitTime(final Duration waitTime) {
        if (waitTime.isNegative()) {
            throw new IllegalArgumentException("Redis lock waitTime must not be negative");
        }
        final long waitMillis = RedisLockRequest.toMillis(waitTime, "waitTime");
        if (waitMillis < 0) {
            throw new IllegalArgumentException("Redis lock waitTime must not be negative");
        }
    }

    private static void validateLeaseTime(final Duration leaseTime) {
        final long leaseMillis = RedisLockRequest.toMillis(leaseTime, "leaseTime");
        if (leaseMillis < RedisLockRequest.MINIMUM_LEASE_TIME.toMillis()) {
            throw new IllegalArgumentException("Redis lock leaseTime must be at least 1 millisecond");
        }
    }

    private static long toMillis(final Duration duration, final String name) {
        try {
            return duration.toMillis();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Redis lock " + name + " is too large", exception);
        }
    }
}

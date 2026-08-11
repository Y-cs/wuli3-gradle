package com.kjs.wuli3.redis.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RedisLockRequestTest {

    @Test
    void createsWatchdogAndFixedLeaseRequests() {
        final RedisLockRequest watchdog = RedisLockRequest.watchdog("orders:1", Duration.ZERO);
        final RedisLockRequest fixed =
                RedisLockRequest.fixedLease("orders:2", Duration.ofSeconds(1), Duration.ofSeconds(5));

        assertThat(watchdog.leaseTime()).isEmpty();
        assertThat(fixed.leaseTime()).contains(Duration.ofSeconds(5));
    }

    @Test
    void rejectsInvalidRequestValues() {
        assertThatThrownBy(() -> RedisLockRequest.watchdog(" ", Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RedisLockRequest.watchdog("orders:1", Duration.ofNanos(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RedisLockRequest.fixedLease("orders:1", Duration.ZERO, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RedisLockRequest.fixedLease("orders:1", Duration.ZERO, Duration.ofNanos(999_999)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

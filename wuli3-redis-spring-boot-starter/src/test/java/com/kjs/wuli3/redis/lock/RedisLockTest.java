package com.kjs.wuli3.redis.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RedisLockTest {

    @Test
    void createsWatchdogAndFixedLeaseRequests() {
        final RedisLock watchdog = RedisLock.watchdog("orders:1", Duration.ZERO);
        final RedisLock fixed = RedisLock.fixedLease("orders:2", Duration.ofSeconds(1), Duration.ofSeconds(5));

        assertThat(watchdog.leaseTime()).isEmpty();
        assertThat(fixed.leaseTime()).contains(Duration.ofSeconds(5));
    }

    @Test
    void rejectsInvalidRequestValues() {
        assertThatThrownBy(() -> RedisLock.watchdog(" ", Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RedisLock.watchdog("orders:1", Duration.ofNanos(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RedisLock.fixedLease("orders:1", Duration.ZERO, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RedisLock.fixedLease("orders:1", Duration.ZERO, Duration.ofNanos(999_999)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

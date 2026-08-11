package com.kjs.wuli3.redis.internal.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.redis.error.RedisLockAcquisitionException;
import com.kjs.wuli3.redis.error.RedisLockInterruptedException;
import com.kjs.wuli3.redis.lock.RedisLockRequest;
import com.kjs.wuli3.redis.lock.RedissonRedisLockExecutor;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class RedissonRedisLockExecutorTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    private RedissonRedisLockExecutor executor;

    @BeforeEach
    void setUp() {
        when(this.redissonClient.getLock("orders:1")).thenReturn(this.lock);
        this.executor = new RedissonRedisLockExecutor(this.redissonClient);
    }

    @Test
    void executesWithWatchdogAndUnlocksOwnedLock() throws InterruptedException {
        final RedisLockRequest request = RedisLockRequest.watchdog("orders:1", Duration.ofSeconds(2));
        final AtomicBoolean called = new AtomicBoolean();
        when(this.lock.tryLock(2_000L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(this.lock.isHeldByCurrentThread()).thenReturn(true);

        assertThat(this.executor.tryExecute(request, () -> called.set(true))).isTrue();

        assertThat(called).isTrue();
        verify(this.lock).unlock();
    }

    @Test
    void usesFixedLeaseAndReturnsSupplierValue() throws InterruptedException {
        final RedisLockRequest request =
                RedisLockRequest.fixedLease("orders:1", Duration.ofSeconds(2), Duration.ofSeconds(5));
        when(this.lock.tryLock(2_000L, 5_000L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(this.lock.isHeldByCurrentThread()).thenReturn(true);

        assertThat(this.executor.tryExecute(request, () -> "done")).contains("done");
    }

    @Test
    void distinguishesContentionForTryAndRequiredExecution() throws InterruptedException {
        final RedisLockRequest request = RedisLockRequest.watchdog("orders:1", Duration.ZERO);
        final AtomicBoolean called = new AtomicBoolean();
        when(this.lock.tryLock(0L, TimeUnit.MILLISECONDS)).thenReturn(false);

        assertThat(this.executor.tryExecute(request, () -> called.set(true))).isFalse();
        assertThatThrownBy(() -> this.executor.execute(request, () -> called.set(true)))
                .isInstanceOf(RedisLockAcquisitionException.class);
        assertThat(called).isFalse();
        verify(this.lock, never()).unlock();
    }

    @Test
    void restoresInterruptStatusAndRaisesTypedException() throws InterruptedException {
        final RedisLockRequest request = RedisLockRequest.watchdog("orders:1", Duration.ofSeconds(1));
        when(this.lock.tryLock(1_000L, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException("stop"));

        try {
            assertThatThrownBy(() -> this.executor.tryExecute(request, () -> "unused"))
                    .isInstanceOf(RedisLockInterruptedException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void preservesBusinessFailureAndSuppressesUnlockFailure() throws InterruptedException {
        final RedisLockRequest request = RedisLockRequest.watchdog("orders:1", Duration.ZERO);
        final IllegalStateException businessFailure = new IllegalStateException("business failed");
        final IllegalStateException unlockFailure = new IllegalStateException("unlock failed");
        when(this.lock.tryLock(0L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(this.lock.isHeldByCurrentThread()).thenReturn(true);
        doThrow(unlockFailure).when(this.lock).unlock();

        assertThatThrownBy(() -> this.executor.execute(request, () -> {
                    throw businessFailure;
                }))
                .isSameAs(businessFailure)
                .satisfies(exception -> assertThat(exception.getSuppressed()).containsExactly(unlockFailure));
    }

    @Test
    void doesNotUnlockWhenCurrentThreadNoLongerOwnsLock() throws InterruptedException {
        final RedisLockRequest request = RedisLockRequest.watchdog("orders:1", Duration.ZERO);
        when(this.lock.tryLock(0L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(this.lock.isHeldByCurrentThread()).thenReturn(false);

        this.executor.execute(request, () -> {});

        verify(this.lock, never()).unlock();
    }
}

package com.kjs.wuli3.redis.lock;

import com.kjs.wuli3.redis.error.RedisLockAcquisitionException;
import com.kjs.wuli3.redis.error.RedisLockInterruptedException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/** 明确定义竞争和中断语义的 Redisson 锁执行器。 */
public final class RedissonRedisLockExecutor implements RedisLockExecutor {

    private final RedissonClient redissonClient;

    public RedissonRedisLockExecutor(final RedissonClient redissonClient) {
        this.redissonClient = Objects.requireNonNull(redissonClient, "redissonClient");
    }

    @Override
    public boolean tryExecute(final RedisLockRequest request, final Runnable action) {
        Objects.requireNonNull(action, "action");
        return this.tryExecute(request, () -> {
                    action.run();
                    return Boolean.TRUE;
                })
                .isPresent();
    }

    @Override
    public <T> Optional<T> tryExecute(final RedisLockRequest request, final Supplier<T> action) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(action, "action");
        final RLock lock = this.redissonClient.getLock(request.key());
        if (!this.tryLock(lock, request)) {
            return Optional.empty();
        }

        Throwable primaryFailure = null;
        try {
            return Optional.of(Objects.requireNonNull(action.get(), "Redis lock action result"));
        } catch (RuntimeException | Error failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            RedissonRedisLockExecutor.release(lock, primaryFailure);
        }
    }

    @Override
    public void execute(final RedisLockRequest request, final Runnable action) {
        if (!this.tryExecute(request, action)) {
            throw new RedisLockAcquisitionException(request.key());
        }
    }

    @Override
    public <T> T execute(final RedisLockRequest request, final Supplier<T> action) {
        return this.tryExecute(request, action).orElseThrow(() -> new RedisLockAcquisitionException(request.key()));
    }

    private boolean tryLock(final RLock lock, final RedisLockRequest request) {
        final long waitMillis = RedissonRedisLockExecutor.toMillis(request.waitTime());
        try {
            final Optional<Duration> leaseTime = request.leaseTime();
            if (leaseTime.isPresent()) {
                return lock.tryLock(
                        waitMillis, RedissonRedisLockExecutor.toMillis(leaseTime.orElseThrow()), TimeUnit.MILLISECONDS);
            }
            return lock.tryLock(waitMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RedisLockInterruptedException(request.key(), exception);
        }
    }

    private static long toMillis(final Duration duration) {
        return duration.toMillis();
    }

    private static void release(final RLock lock, final @Nullable Throwable primaryFailure) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (RuntimeException | Error releaseFailure) {
            if (primaryFailure != null) {
                primaryFailure.addSuppressed(releaseFailure);
                return;
            }
            throw releaseFailure;
        }
    }
}

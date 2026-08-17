package com.kjs.wuli3.redis.lock;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 在当前线程持有 Redisson 分布式锁期间执行任务。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface RedisLockExecutor {

    /** 尝试获取锁，并返回任务是否执行。 */
    boolean tryExecute(RedisLockRequest request, Runnable action);

    /** 尝试获取锁，并在任务执行时返回结果。 */
    <T> Optional<T> tryExecute(RedisLockRequest request, Supplier<T> action);

    /** 获取锁并执行任务，等待超时后抛出异常。 */
    void execute(RedisLockRequest request, Runnable action);

    /** 获取锁并返回任务结果，等待超时后抛出异常。 */
    <T> T execute(RedisLockRequest request, Supplier<T> action);
}

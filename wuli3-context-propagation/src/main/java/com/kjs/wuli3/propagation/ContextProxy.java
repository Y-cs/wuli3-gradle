package com.kjs.wuli3.propagation;

import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/** 捕获当前调用上下文，并在后续执行中临时恢复它。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface ContextProxy {

    /**
     * 捕获当前调用上下文的独立快照。
     *
     * @return 可在后续恢复的上下文快照
     */
    ContextSnapshot capture();

    /**
     * 将指定快照恢复到当前调用，并返回用于结束恢复作用域的句柄。
     *
     * <p>调用方应通过 try-with-resources 关闭返回的作用域，以恢复先前上下文。
     *
     * @param snapshot 待恢复的上下文快照
     * @return 用于恢复先前上下文的作用域
     */
    ContextScope restore(ContextSnapshot snapshot);

    /**
     * 捕获当前上下文，并返回执行时自动恢复该上下文的任务包装器。
     *
     * @param task 原始任务
     * @return 绑定捕获快照的任务包装器
     * @throws NullPointerException 当 {@code task} 为 {@code null} 时
     */
    default Runnable wrap(final Runnable task) {
        Objects.requireNonNull(task, "task");
        final ContextSnapshot snapshot = this.capture();
        return () -> {
            try (ContextScope scope = this.restore(snapshot)) {
                task.run();
            }
        };
    }

    /**
     * 捕获当前上下文，并返回执行时自动恢复该上下文的可调用任务包装器。
     *
     * @param task 原始可调用任务
     * @param <T> 任务返回值类型
     * @return 绑定捕获快照的可调用任务包装器
     * @throws NullPointerException 当 {@code task} 为 {@code null} 时
     */
    default <T> Callable<T> wrap(final Callable<T> task) {
        Objects.requireNonNull(task, "task");
        final ContextSnapshot snapshot = this.capture();
        return () -> {
            try (ContextScope scope = this.restore(snapshot)) {
                return task.call();
            }
        };
    }

    /**
     * 捕获当前上下文，并返回执行时自动恢复该上下文的供应器包装器。
     *
     * @param supplier 原始供应器
     * @param <T> 供应结果类型
     * @return 绑定捕获快照的供应器包装器
     * @throws NullPointerException 当 {@code supplier} 为 {@code null} 时
     */
    default <T> Supplier<T> wrapSupplier(final Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        final ContextSnapshot snapshot = this.capture();
        return () -> {
            try (ContextScope scope = this.restore(snapshot)) {
                return supplier.get();
            }
        };
    }
}

package com.kjs.wuli3.propagation.snapshot;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 捕获并恢复当前调用上下文。
 */
public interface ContextPropagator {

    ContextSnapshot capture();

    ContextScope restore(ContextSnapshot snapshot);

    default Runnable wrap(final Runnable task) {
        Objects.requireNonNull(task, "task");
        final ContextSnapshot snapshot = this.capture();
        return () -> {
            final ContextScope scope = this.restore(snapshot);
            try {
                task.run();
            } finally {
                scope.close();
            }
        };
    }

    default <T> Callable<T> wrap(final Callable<T> task) {
        Objects.requireNonNull(task, "task");
        final ContextSnapshot snapshot = this.capture();
        return () -> {
            final ContextScope scope = this.restore(snapshot);
            try {
                return task.call();
            } finally {
                scope.close();
            }
        };
    }

    default <T> Supplier<T> wrapSupplier(final Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        final ContextSnapshot snapshot = this.capture();
        return () -> {
            final ContextScope scope = this.restore(snapshot);
            try {
                return supplier.get();
            } finally {
                scope.close();
            }
        };
    }
}

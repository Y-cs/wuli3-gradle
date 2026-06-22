package com.kjs.wuli3.core.function;

import com.kjs.wuli3.core.error.CommonErrorCode;
import com.kjs.wuli3.core.error.SystemException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Functions {
    private Functions() {
    }

    public static <T> Consumer<T> uncheckedConsumer(CheckedConsumer<T> consumer) {
        return value -> {
            try {
                consumer.accept(value);
            } catch (Exception ex) {
                throw wrap(ex);
            }
        };
    }

    public static <T, R> Function<T, R> uncheckedFunction(CheckedFunction<T, R> function) {
        return value -> {
            try {
                return function.apply(value);
            } catch (Exception ex) {
                throw wrap(ex);
            }
        };
    }

    public static <T> Supplier<T> uncheckedSupplier(CheckedSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception ex) {
                throw wrap(ex);
            }
        };
    }

    private static RuntimeException wrap(Exception ex) {
        if (ex instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        String message = ex.getMessage() == null ? CommonErrorCode.INTERNAL_ERROR.message() : ex.getMessage();
        return new SystemException(CommonErrorCode.INTERNAL_ERROR, message, ex);
    }
}

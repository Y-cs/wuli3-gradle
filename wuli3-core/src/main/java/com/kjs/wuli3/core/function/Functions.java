package com.kjs.wuli3.core.function;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;

import java.util.function.Supplier;

public final class Functions {

    private Functions() {
    }

    public static <T> Supplier<T> uncheckedSupplier(CheckedSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (ErrorCodeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new ErrorCodeException(SystemErrors.INTERNAL_ERROR, message(ex), ex);
            }
        };
    }

    private static String message(Exception ex) {
        String message = ex.getMessage();
        return message == null ? ex.getClass()
                .getName() : message;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}

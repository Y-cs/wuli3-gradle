package com.kjs.wuli3.core.assertion;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;
import java.util.Collection;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Assertion helpers that fail with the project's {@link ErrorCodeException} model.
 */
public final class Asserts {

    private Asserts() {}

    public static AssertException isTrue(final boolean condition) {
        return () -> !condition;
    }

    public static AssertException notNull(final @Nullable Object obj) {
        return () -> obj == null;
    }

    public static AssertException notBlank(final @Nullable String value) {
        return () -> value == null || value.isBlank();
    }

    public static AssertException notEmpty(final @Nullable String value) {
        return () -> value == null || value.isEmpty();
    }

    public static AssertException isEmptyCollection(final @Nullable Collection<?> collection) {
        return () -> collection != null && !collection.isEmpty();
    }

    public static AssertException isNotEmptyCollection(final @Nullable Collection<?> collection) {
        return () -> collection == null || collection.isEmpty();
    }

    public static AssertException isEmptyMap(final @Nullable Map<?, ?> map) {
        return () -> map != null && !map.isEmpty();
    }

    public static AssertException isNotEmptyMap(final @Nullable Map<?, ?> map) {
        return () -> map == null || map.isEmpty();
    }

    @FunctionalInterface
    public interface AssertException {

        boolean condition();

        default AssertException reversed() {
            return () -> !condition();
        }

        default void throwException(final ErrorCode errorCode) {
            if (condition()) {
                throw new ErrorCodeException(errorCode);
            }
        }

        default void throwException(final ErrorCode errorCode, final String message) {
            if (condition()) {
                throw new ErrorCodeException(errorCode, message);
            }
        }

        default void throwIllegalArgumentException(final String message) {
            if (condition()) {
                throw new ErrorCodeException(SystemErrors.ILLEGAL_ARGUMENT, message);
            }
        }
    }
}

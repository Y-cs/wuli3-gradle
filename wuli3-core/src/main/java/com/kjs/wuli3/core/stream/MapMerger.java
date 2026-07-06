package com.kjs.wuli3.core.stream;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MapMerger {

    private MapMerger() {
    }

    public static <V> V keepFirstValue(final V left, final V right) {
        return left;
    }

    public static <V> V keepLastValue(final V left, final V right) {
        return right;
    }

    public static <V> @Nullable V keepFirstNonNullValue(final @Nullable V left, final @Nullable V right) {
        return left == null ? right : left;
    }

    public static <V> V throwDuplicate(final V left, final V right) {
        throw new ErrorCodeException(SystemErrors.ILLEGAL_STATE, "Duplicate key values " + left + " and " + right);
    }
}

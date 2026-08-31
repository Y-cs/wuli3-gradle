package com.kjs.wuli3.core.stream;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.builtin.CommonErrors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 提供 Map 收集器使用的常见键冲突合并策略。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@NullMarked
public final class MapMerger {

    private MapMerger() {}

    /** 保留第一个值。 */
    public static <V> V keepFirstValue(final V left, final V right) {
        return left;
    }

    /** 保留最后一个值。 */
    public static <V> V keepLastValue(final V left, final V right) {
        return right;
    }

    /** 返回第一个非空值；两个值都为空时返回空。 */
    public static <V> @Nullable V keepFirstNonNullValue(final @Nullable V left, final @Nullable V right) {
        return left == null ? right : left;
    }

    /** 发现重复键时抛出非法状态异常。 */
    public static <V> V throwDuplicate(final V left, final V right) {
        throw new ErrorCodeException(CommonErrors.ILLEGAL_STATE, "Duplicate key values " + left + " and " + right);
    }
}

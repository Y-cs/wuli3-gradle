package com.kjs.wuli3.core.assertion;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.builtin.CommonErrors;
import java.util.Collection;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * 基于项目 {@link ErrorCodeException} 错误模型的断言工具。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class Asserts {

    private Asserts() {}

    /** 在条件为假时返回失败断言。 */
    public static AssertCondition isTrue(final boolean condition) {
        return () -> !condition;
    }

    /** 在对象为空时返回失败断言。 */
    public static AssertCondition notNull(final @Nullable Object obj) {
        return () -> obj == null;
    }

    /** 在字符串为空或仅包含空白字符时返回失败断言。 */
    public static AssertCondition notBlank(final @Nullable String value) {
        return () -> value == null || value.isBlank();
    }

    /** 在字符串为空或长度为零时返回失败断言。 */
    public static AssertCondition notEmpty(final @Nullable String value) {
        return () -> value == null || value.isEmpty();
    }

    /** 在集合非空时返回失败断言。 */
    public static AssertCondition isEmptyCollection(final @Nullable Collection<?> collection) {
        return () -> collection != null && !collection.isEmpty();
    }

    /** 在集合为空时返回失败断言。 */
    public static AssertCondition isNotEmptyCollection(final @Nullable Collection<?> collection) {
        return () -> collection == null || collection.isEmpty();
    }

    /** 在 Map 非空时返回失败断言。 */
    public static AssertCondition isEmptyMap(final @Nullable Map<?, ?> map) {
        return () -> map != null && !map.isEmpty();
    }

    /** 在 Map 为空时返回失败断言。 */
    public static AssertCondition isNotEmptyMap(final @Nullable Map<?, ?> map) {
        return () -> map == null || map.isEmpty();
    }

    @FunctionalInterface
    public interface AssertCondition {

        /** 返回当前断言条件是否成立。 */
        boolean condition();

        /** 返回反转当前条件后的断言。 */
        default AssertCondition reversed() {
            return () -> !condition();
        }

        /** 条件成立时抛出指定错误码异常。 */
        default void throwException(final ErrorCode errorCode) {
            if (condition()) {
                throw new ErrorCodeException(errorCode);
            }
        }

        /** 条件成立时抛出带自定义消息的错误码异常。 */
        default void throwException(final ErrorCode errorCode, final String message) {
            if (condition()) {
                throw new ErrorCodeException(errorCode, message);
            }
        }

        /** 条件成立时以非法参数错误码抛出异常。 */
        default void throwIllegalArgumentException(final String message) {
            if (condition()) {
                throw new ErrorCodeException(CommonErrors.ILLEGAL_ARGUMENT, message);
            }
        }
    }
}

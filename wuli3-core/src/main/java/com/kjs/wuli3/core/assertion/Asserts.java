package com.kjs.wuli3.core.assertion;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.builtin.CommonErrors;
import com.kjs.wuli3.core.error.model.ErrorCode;
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

    /** 当条件为假时触发失败断言。 */
    public static AssertCondition whenFalse(final boolean condition) {
        return () -> !condition;
    }

    /** 当条件为真时触发失败断言。 */
    public static AssertCondition whenTrue(final boolean condition) {
        return () -> condition;
    }

    /** 当对象为 null 时触发失败断言。 */
    public static AssertCondition whenNull(final @Nullable Object obj) {
        return () -> obj == null;
    }

    /** 当对象不为 null 时触发失败断言。 */
    public static AssertCondition whenNotNull(final @Nullable Object obj) {
        return () -> obj != null;
    }

    /** 当字符串为空或仅包含空白字符时触发失败断言。 */
    public static AssertCondition whenBlank(final @Nullable String value) {
        return () -> value == null || value.isBlank();
    }

    /** 当字符串非空且包含非空白字符时触发失败断言。 */
    public static AssertCondition whenNotBlank(final @Nullable String value) {
        return () -> value != null && !value.isBlank();
    }

    /** 当字符串为空或长度为零时触发失败断言。 */
    public static AssertCondition whenEmpty(final @Nullable String value) {
        return () -> value == null || value.isEmpty();
    }

    /** 当字符串非空且长度大于零时触发失败断言。 */
    public static AssertCondition whenNotEmpty(final @Nullable String value) {
        return () -> value != null && !value.isEmpty();
    }

    /** 当集合为空时触发失败断言。 */
    public static AssertCondition whenEmptyCollection(final @Nullable Collection<?> collection) {
        return () -> collection == null || collection.isEmpty();
    }

    /** 当集合非空时触发失败断言。 */
    public static AssertCondition whenNotEmptyCollection(final @Nullable Collection<?> collection) {
        return () -> collection != null && !collection.isEmpty();
    }

    /** 当 Map 为空时触发失败断言。 */
    public static AssertCondition whenEmptyMap(final @Nullable Map<?, ?> map) {
        return () -> map == null || map.isEmpty();
    }

    /** 当 Map 非空时触发失败断言。 */
    public static AssertCondition whenNotEmptyMap(final @Nullable Map<?, ?> map) {
        return () -> map != null && !map.isEmpty();
    }

    @FunctionalInterface
    public interface AssertCondition {

        /** 返回当前失败条件是否成立（即是否应该抛出异常）。 */
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

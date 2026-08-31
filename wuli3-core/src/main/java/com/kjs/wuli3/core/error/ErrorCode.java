package com.kjs.wuli3.core.error;

import java.io.Serializable;
import java.util.Locale;

/**
 * 可由 {@link com.kjs.wuli3.core.error.ErrorCodeException} 携带的稳定错误标识。
 *
 * <p>本地错误通常由带元数据的枚举实现；跨进程接收的错误由序列化值对象实现，避免依赖提供方业务枚举。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface ErrorCode extends Serializable {

    /** 返回该错误标识对应的默认消息。 */
    String getMessage();

    /**
     * 返回指定语言环境的错误消息。
     *
     * <p>默认实现返回 {@link #getMessage()}。如需国际化支持，建议使用 MessageSource 或 ResourceBundle。
     *
     * @param locale 目标语言环境
     * @return 本地化的错误消息，如无对应翻译则返回默认消息
     */
    default String getMessage(final Locale locale) {
        return this.getMessage();
    }

    /** 返回错误名称；本地枚举默认使用常量名。 */
    default String getName() {
        if (this instanceof Enum<?> errorEnum) {
            return errorEnum.name();
        }
        return this.getClass().getSimpleName();
    }

    /** 返回声明错误的类型；远程错误返回其传播协议类型。 */
    default Class<? extends ErrorCode> getErrorType() {
        if (this instanceof Enum<?> errorEnum) {
            final Class<?> declaringClass = errorEnum.getDeclaringClass();
            return declaringClass.asSubclass(ErrorCode.class);
        }
        return this.getClass();
    }
}

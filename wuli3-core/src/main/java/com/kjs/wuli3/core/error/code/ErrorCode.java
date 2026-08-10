package com.kjs.wuli3.core.error.code;

import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import java.io.Serializable;

/**
 * 由枚举常量实现的稳定错误标识。
 *
 * <p>实现类型必须是枚举，以保证错误名称、所属模块和常量级策略可被确定性解析。
 */
public interface ErrorCode extends Serializable {

    String getMessage();

    default String getName() {
        return this.enumValue().name();
    }

    default Class<?> getErrorType() {
        return this.enumValue().getDeclaringClass();
    }

    private Enum<?> enumValue() {
        if (this instanceof Enum<?> errorEnum) {
            return errorEnum;
        }
        throw new ErrorCodeException(ErrorFrameworkErrors.INVALID_ERROR_CODE).severity(ErrorSeverity.WARNING);
    }
}

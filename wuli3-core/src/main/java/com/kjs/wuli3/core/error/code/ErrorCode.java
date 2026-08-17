package com.kjs.wuli3.core.error.code;

import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import java.io.Serializable;

/**
 * 由枚举常量实现的稳定错误标识。
 *
 * <p>实现类型必须是枚举，以保证错误名称、所属模块和常量级策略可被确定性解析。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface ErrorCode extends Serializable {

    /** 返回对外展示的默认错误消息。 */
    String getMessage();

    /** 返回枚举常量名称。 */
    default String getName() {
        return this.enumValue().name();
    }

    /** 返回声明该错误码的枚举类型。 */
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

package com.kjs.wuli3.core.exception;

import java.io.Serializable;

public interface ErrorCode extends Serializable {

    String getMessage();

    default String getName() {
        if (this instanceof Enum<?> errorEnum) {
            return errorEnum.name();
        }
        throw new ErrorCodeException(ErrorFrameworkErrors.INVALID_ERROR_CODE).severity(ErrorSeverity.WARNING);
    }

    default Class<?> getErrorType() {
        if (this instanceof Enum<?> errorEnum) {
            return errorEnum.getDeclaringClass();
        }
        throw new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND).severity(ErrorSeverity.WARNING);
    }

}

package com.kjs.wuli3.core.error;

import java.io.Serializable;

/**
 * Stable error identity implemented by enum constants.
 *
 * <p>Implementations must be enums so names, declaring modules, and constant-level policies remain deterministic.
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

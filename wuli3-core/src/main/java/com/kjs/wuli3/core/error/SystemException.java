package com.kjs.wuli3.core.error;

import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class SystemException extends ApplicationException {
    private static final long serialVersionUID = 1L;

    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SystemException(ErrorCode errorCode, String message, @Nullable Throwable cause) {
        super(errorCode, message, cause);
    }

    public SystemException(ErrorCode errorCode, String message, @Nullable Throwable cause, Map<String, ?> context) {
        super(errorCode, message, cause, context);
    }
}

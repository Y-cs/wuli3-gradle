package com.kjs.wuli3.core.error;

import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public abstract class ApplicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final transient Map<String, Object> context;

    protected ApplicationException(ErrorCode errorCode) {
        this(errorCode, errorCode.message(), null, Map.of());
    }

    protected ApplicationException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, Map.of());
    }

    protected ApplicationException(ErrorCode errorCode, String message, @Nullable Throwable cause) {
        this(errorCode, message, cause, Map.of());
    }

    protected ApplicationException(
            ErrorCode errorCode,
            String message,
            @Nullable Throwable cause,
            Map<String, ?> context
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = Collections.unmodifiableMap(new LinkedHashMap<>(context));
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Map<String, Object> context() {
        return context;
    }
}

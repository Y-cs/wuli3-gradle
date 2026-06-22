package com.kjs.wuli3.core.error;

public enum CommonErrorCode implements ErrorCode {
    SUCCESS("0", "success"),
    BAD_REQUEST("400", "Bad request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not found"),
    VALIDATION_FAILED("VALIDATION_FAILED", "Validation failed"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}

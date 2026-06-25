package com.kjs.wuli3.core.error;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * ErrorModuleHolder
 *
 * @author GuoYang create on 2026/6/24 11:17
 */
public class ErrorModuleHolder {

    private final Map<Class<?>, ErrorModule> errorCodeCache = Maps.newConcurrentMap();

    public ErrorModule getErrorModule(ErrorCode errorCode) {
        final Class<?> errorType = errorCode.getErrorType();
        return errorCodeCache.computeIfAbsent(errorType, type -> {
            final ErrorModule errorModule = type.getAnnotation(ErrorModule.class);
            if (errorModule == null) {
                throw new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND);
            }
            return errorModule;
        });
    }

    public ErrorPolicy getErrorPolicy(ErrorCode errorCode) {
        final ErrorModule errorModule = getErrorModule(errorCode);
        return new ErrorPolicy(errorModule.severity(), errorModule.visibility());
    }

    private ErrorModuleHolder() {
    }

    public static ErrorModuleHolder instance() {
        return ErrorModuleHolderInstance.INSTANCE;
    }

    private static class ErrorModuleHolderInstance {
        private static final ErrorModuleHolder INSTANCE = new ErrorModuleHolder();
    }

}

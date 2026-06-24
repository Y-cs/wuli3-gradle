package com.kjs.wuli3.web.error;

import com.google.common.collect.Maps;
import com.kjs.wuli3.core.exception.ErrorCode;
import com.kjs.wuli3.core.exception.ErrorCodeResolver;
import com.kjs.wuli3.core.exception.ErrorModule;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * DefaultErrorCodeResolver
 *
 * @author GuoYang create on 2026/6/24 11:17
 */
public class WebErrorCodeResolver implements ErrorCodeResolver {

    private final Map<Class<?>, ErrorModule> errorCodeCache = Maps.newConcurrentMap();

    private WebErrorCodeResolver() {}

    public static WebErrorCodeResolver instance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final WebErrorCodeResolver INSTANCE = new WebErrorCodeResolver();
    }

    @Override
    public String formatErrorCode(ErrorCode errorCode) {
        return Optional.of(this.getErrorModule(errorCode))
                .map(ErrorModule::value)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(moduleName -> moduleName + "." + errorCode.getName())
                .map(String::toUpperCase)
                .orElseThrow(() -> new IllegalArgumentException("Error code module name is blank"));
    }

    private ErrorModule getErrorModule(ErrorCode errorCode) {
        final Class<?> errorType = errorCode.getErrorType();
        return errorCodeCache.computeIfAbsent(errorType, type -> type.getAnnotation(ErrorModule.class));
    }
}

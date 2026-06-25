package com.kjs.wuli3.web.error;

import com.google.common.collect.Maps;
import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.core.error.ErrorFrameworkErrors;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.core.error.ErrorSeverity;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 使用错误码所属模块前缀格式化错误码。
 */
public class WebErrorCodeResolver implements ErrorCodeResolver {

    private final Map<Class<?>, ErrorModule> errorCodeCache = Maps.newConcurrentMap();

    @Override
    public String formatErrorCode(ErrorCode errorCode) {
        return Optional.of(this.getErrorModule(errorCode))
                .map(ErrorModule::value)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(moduleName -> moduleName + "." + errorCode.getName())
                .map(errorCodeValue -> errorCodeValue.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND).severity(
                        ErrorSeverity.WARNING));
    }

    private ErrorModule getErrorModule(ErrorCode errorCode) {
        final Class<?> errorType = errorCode.getErrorType();
        return errorCodeCache.computeIfAbsent(errorType, type -> type.getAnnotation(ErrorModule.class));
    }
}

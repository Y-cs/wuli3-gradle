package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.code.ErrorCodeResolver;
import com.kjs.wuli3.core.error.code.ErrorFrameworkErrors;
import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.core.error.metadata.ErrorMetadataParser;
import com.kjs.wuli3.core.error.metadata.ErrorModule;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.web.error.ApplicationServiceProperties;
import java.util.Locale;
import java.util.Optional;

/**
 * 将对外 Web 错误码格式化为 SERVICE.MODULE.ERROR_NAME。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public class WebErrorCodeResolver implements ErrorCodeResolver {

    private final String serviceCode;

    public WebErrorCodeResolver(final ApplicationServiceProperties applicationServiceProperties) {
        this.serviceCode = Optional.of(applicationServiceProperties)
                .map(ApplicationServiceProperties::getServiceCode)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .orElse("");
    }

    @Override
    public String resolve(final ErrorCode errorCode) {
        final ErrorModule errorModule = ErrorMetadataParser.instance().getErrorModule(errorCode);
        return Optional.of(errorModule)
                .map(ErrorModule::value)
                .filter(value -> !value.isBlank())
                .map(String::trim)
                .map(moduleName -> this.prefix() + moduleName + "." + errorCode.getName())
                .map(v -> v.toUpperCase(Locale.ROOT))
                .orElseThrow(() ->
                        new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND).severity(ErrorSeverity.WARNING));
    }

    private String prefix() {
        return this.serviceCode.isBlank() ? "" : this.serviceCode + ".";
    }
}

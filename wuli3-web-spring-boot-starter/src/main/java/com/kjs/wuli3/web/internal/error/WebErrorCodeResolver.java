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
 * Formats external web error codes as SERVICE.MODULE.ERROR_NAME.
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

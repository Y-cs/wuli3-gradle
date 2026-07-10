package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.core.error.ErrorFrameworkErrors;
import com.kjs.wuli3.core.error.ErrorMetadataParser;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.web.config.properties.ApplicationServiceProperties;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Formats external web error codes as SERVICE.MODULE.ERROR_NAME.
 */
public class WebErrorCodeResolver implements ErrorCodeResolver {

    private final String serviceCode;

    public WebErrorCodeResolver(final ApplicationServiceProperties applicationServiceProperties) {
        this.serviceCode = Optional.of(applicationServiceProperties)
                .map(ApplicationServiceProperties::getServiceCode)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .orElse("");
    }

    @Override
    public String resolve(final ErrorCode errorCode) {
        final ErrorModule errorModule = ErrorMetadataParser.instance().getErrorModule(errorCode);
        return Optional.of(errorModule)
                .map(ErrorModule::value)
                .filter(StringUtils::isNotBlank)
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

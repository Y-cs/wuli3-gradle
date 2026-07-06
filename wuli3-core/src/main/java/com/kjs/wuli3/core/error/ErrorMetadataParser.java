package com.kjs.wuli3.core.error;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

/**
 * Parses and caches error metadata declared on {@link ErrorCode} enum types and constants.
 */
public final class ErrorMetadataParser {

    private static final ErrorMetadataParser INSTANCE = new ErrorMetadataParser();

    private final Map<Class<?>, ErrorModule> errorCodeCache = Maps.newConcurrentMap();
    private final Map<ErrorCode, ResolvedErrorPolicy> policyCache = Maps.newConcurrentMap();

    private ErrorMetadataParser() {
    }

    public static ErrorMetadataParser instance() {
        return INSTANCE;
    }

    public ErrorModule getErrorModule(final ErrorCode errorCode) {
        final Class<?> errorType = errorCode.getErrorType();
        return this.errorCodeCache.computeIfAbsent(errorType, type -> {
            final ErrorModule errorModule = type.getAnnotation(ErrorModule.class);
            if (errorModule == null) {
                throw new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND);
            }
            return errorModule;
        });
    }

    public ResolvedErrorPolicy getErrorPolicy(final ErrorCode errorCode) {
        return this.policyCache.computeIfAbsent(errorCode, code -> {
            if (code instanceof Enum<?> errorEnum) {
                final Optional<ErrorPolicy> fieldPolicy = fieldPolicy(errorEnum);
                if (fieldPolicy.isPresent()) {
                    return ResolvedErrorPolicy.from(fieldPolicy.get());
                }
            }
            final ErrorModule errorModule = this.getErrorModule(code);
            return ResolvedErrorPolicy.from(errorModule.policy());
        });
    }

    private static Optional<ErrorPolicy> fieldPolicy(final Enum<?> errorEnum) {
        try {
            final Field field = errorEnum.getDeclaringClass()
                    .getField(errorEnum.name());
            return Optional.ofNullable(field.getAnnotation(ErrorPolicy.class));
        } catch (NoSuchFieldException ex) {
            throw new ErrorCodeException(ErrorFrameworkErrors.ERROR_CODE_RESOLVE_FAILED, ex);
        }
    }
}

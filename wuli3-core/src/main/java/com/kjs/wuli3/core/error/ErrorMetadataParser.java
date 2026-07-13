package com.kjs.wuli3.core.error;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parses and caches error metadata declared on {@link ErrorCode} enum types and constants.
 */
public final class ErrorMetadataParser {

    private static final ErrorMetadataParser INSTANCE = new ErrorMetadataParser();

    private final Map<Class<?>, ErrorModule> moduleCache = new ConcurrentHashMap<>();
    private final Map<ErrorCode, ResolvedErrorPolicy> policyCache = new ConcurrentHashMap<>();

    private ErrorMetadataParser() {}

    public static ErrorMetadataParser instance() {
        return INSTANCE;
    }

    public ErrorModule getErrorModule(final ErrorCode errorCode) {
        final Class<?> errorType = ErrorMetadataParser.enumValue(errorCode).getDeclaringClass();
        return this.moduleCache.computeIfAbsent(errorType, type -> {
            final ErrorModule errorModule = type.getAnnotation(ErrorModule.class);
            if (errorModule == null) {
                throw new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND);
            }
            if (errorModule.value().isBlank()) {
                throw new ErrorCodeException(ErrorFrameworkErrors.INVALID_ERROR_MODULE);
            }
            return errorModule;
        });
    }

    public ResolvedErrorPolicy getErrorPolicy(final ErrorCode errorCode) {
        ErrorMetadataParser.enumValue(errorCode);
        return this.policyCache.computeIfAbsent(errorCode, code -> {
            final Optional<ErrorPolicy> fieldPolicy =
                    ErrorMetadataParser.fieldPolicy(ErrorMetadataParser.enumValue(code));
            if (fieldPolicy.isPresent()) {
                return ResolvedErrorPolicy.from(fieldPolicy.get());
            }
            final ErrorModule errorModule = this.getErrorModule(code);
            return ResolvedErrorPolicy.from(errorModule.policy());
        });
    }

    private static Enum<?> enumValue(final ErrorCode errorCode) {
        if (errorCode instanceof Enum<?> errorEnum) {
            return errorEnum;
        }
        throw new ErrorCodeException(ErrorFrameworkErrors.INVALID_ERROR_CODE).severity(ErrorSeverity.WARNING);
    }

    private static Optional<ErrorPolicy> fieldPolicy(final Enum<?> errorEnum) {
        try {
            final Field field = errorEnum.getDeclaringClass().getField(errorEnum.name());
            return Optional.ofNullable(field.getAnnotation(ErrorPolicy.class));
        } catch (NoSuchFieldException ex) {
            throw new ErrorCodeException(ErrorFrameworkErrors.ERROR_CODE_RESOLVE_FAILED, ex);
        }
    }
}

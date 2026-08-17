package com.kjs.wuli3.core.error.metadata;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.code.ErrorFrameworkErrors;
import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ResolvedErrorPolicy;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 解析并缓存错误码枚举及常量上声明的元数据。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ErrorMetadataParser {

    private static final ErrorMetadataParser INSTANCE = new ErrorMetadataParser();

    private final Map<Class<?>, ErrorModule> moduleCache = new ConcurrentHashMap<>();
    private final Map<ErrorCode, ResolvedErrorPolicy> policyCache = new ConcurrentHashMap<>();

    private ErrorMetadataParser() {}

    /** 返回进程级共享的错误元数据解析器。 */
    public static ErrorMetadataParser instance() {
        return INSTANCE;
    }

    /** 解析错误码所属模块并缓存结果。 */
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

    /** 解析错误码生效的错误策略并缓存结果。 */
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

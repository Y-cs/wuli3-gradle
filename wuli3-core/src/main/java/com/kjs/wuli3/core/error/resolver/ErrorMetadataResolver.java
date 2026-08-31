package com.kjs.wuli3.core.error.resolver;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorMetadata;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.core.error.ErrorOrigin;
import com.kjs.wuli3.core.error.ErrorPropagationProtocol;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.builtin.ErrorFrameworkErrors;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析并缓存错误码枚举及常量上声明的元数据。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ErrorMetadataResolver {

    private static final ErrorMetadataResolver INSTANCE = new ErrorMetadataResolver();

    private final Map<Class<?>, ErrorModule> moduleCache = new ConcurrentHashMap<>();
    private final Map<ErrorCode, ErrorOrigin> originCache = new ConcurrentHashMap<>();
    private final Map<ErrorCode, ErrorSeverity> severityCache = new ConcurrentHashMap<>();
    private final Map<ErrorCode, ErrorVisibility> visibilityCache = new ConcurrentHashMap<>();

    private ErrorMetadataResolver() {}

    /**
     * 返回进程级共享的错误元数据解析器。
     */
    public static ErrorMetadataResolver instance() {
        return INSTANCE;
    }

    /**
     * 解析本地枚举错误码所属模块并缓存结果。
     *
     * @throws ErrorCodeException 如果传入非枚举错误码（如 {@link ErrorPropagationProtocol}）
     */
    public ErrorModule getErrorModule(final ErrorCode errorCode) {
        final Class<?> errorType = ErrorMetadataResolver.enumValue(errorCode).getDeclaringClass();
        return this.moduleCache.computeIfAbsent(errorType, type -> {
            final ErrorModule errorModule = type.getAnnotation(ErrorModule.class);
            if (errorModule == null) {
                throw new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND);
            }
            final String moduleName = errorModule.name().trim();
            if (moduleName.isBlank()) {
                throw new ErrorCodeException(ErrorFrameworkErrors.INVALID_ERROR_MODULE);
            }
            if (!moduleName.matches("[A-Z_]+")) {
                throw new ErrorCodeException(
                        ErrorFrameworkErrors.INVALID_ERROR_MODULE,
                        "Module name must contain only uppercase letters and underscores: " + moduleName);
            }
            return errorModule;
        });
    }

    /**
     * 解析错误的责任归属并缓存结果。
     */
    public ErrorOrigin getOrigin(final ErrorCode errorCode) {
        if (errorCode instanceof ErrorPropagationProtocol protocol) {
            return protocol.origin();
        }
        // 校验必须为枚举类型
        final Enum<?> errorEnum = ErrorMetadataResolver.enumValue(errorCode);
        return this.originCache.computeIfAbsent(errorCode, code -> {
            final Optional<ErrorMetadata> fieldMetadata = ErrorMetadataResolver.fieldMetadata(errorEnum);
            if (fieldMetadata.isPresent()) {
                return fieldMetadata.get().origin();
            }
            final ErrorModule module = this.getErrorModule(code);
            final ErrorMetadata classMetadata = errorEnum.getDeclaringClass().getAnnotation(ErrorMetadata.class);
            return classMetadata == null ? module.defaultMetadata().origin() : classMetadata.origin();
        });
    }

    /**
     * 解析错误的严重程度并缓存结果。
     */
    public ErrorSeverity getSeverity(final ErrorCode errorCode) {
        if (errorCode instanceof ErrorPropagationProtocol protocol) {
            return protocol.severity();
        }
        // 校验必须为枚举类型
        final Enum<?> errorEnum = ErrorMetadataResolver.enumValue(errorCode);
        return this.severityCache.computeIfAbsent(errorCode, code -> {
            final Optional<ErrorMetadata> fieldMetadata = ErrorMetadataResolver.fieldMetadata(errorEnum);
            if (fieldMetadata.isPresent()) {
                return fieldMetadata.get().severity();
            }
            final ErrorModule module = this.getErrorModule(code);
            final ErrorMetadata classMetadata = errorEnum.getDeclaringClass().getAnnotation(ErrorMetadata.class);
            return classMetadata == null ? module.defaultMetadata().severity() : classMetadata.severity();
        });
    }

    /**
     * 解析错误在边界传播时的可见性并缓存结果。
     *
     * <p>查找顺序：字段级注解 → 类级注解 → 模块级注解（默认 PUBLIC）。
     */
    public ErrorVisibility getVisibility(final ErrorCode errorCode) {
        if (errorCode instanceof ErrorPropagationProtocol) {
            return ErrorVisibility.PUBLIC;
        }
        final Enum<?> errorEnum = ErrorMetadataResolver.enumValue(errorCode);
        return this.visibilityCache.computeIfAbsent(errorCode, code -> {
            final Optional<ErrorMetadata> fieldMetadata = ErrorMetadataResolver.fieldMetadata(errorEnum);
            if (fieldMetadata.isPresent()) {
                return fieldMetadata.get().visibility();
            }
            final ErrorMetadata classMetadata = errorEnum.getDeclaringClass().getAnnotation(ErrorMetadata.class);
            if (classMetadata != null) {
                return classMetadata.visibility();
            }
            final ErrorModule module = this.getErrorModule(code);
            return module.defaultMetadata().visibility();
        });
    }

    private static Enum<?> enumValue(final ErrorCode errorCode) {
        if (errorCode instanceof Enum<?> errorEnum) {
            return errorEnum;
        }
        throw new ErrorCodeException(ErrorFrameworkErrors.INVALID_ERROR_CODE);
    }

    private static Optional<ErrorMetadata> fieldMetadata(final Enum<?> errorEnum) {
        try {
            final Field field = errorEnum.getDeclaringClass().getField(errorEnum.name());
            return Optional.ofNullable(field.getAnnotation(ErrorMetadata.class));
        } catch (NoSuchFieldException ex) {
            throw new ErrorCodeException(ErrorFrameworkErrors.ERROR_CODE_RESOLVE_FAILED, ex);
        }
    }
}

package com.kjs.wuli3.core.error.resolver;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.builtin.SystemErrors;
import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorModule;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import com.kjs.wuli3.core.error.model.ErrorVisibility;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier;
import java.util.Locale;
import java.util.Objects;

/**
 * 统一解析稳定错误码，并生成服务边界可传播的错误值。
 *
 * @author GuoYang create on 2026/9/24 10:00
 */
public final class ErrorResolver {

    private static final String INTERNAL_MESSAGE = SystemErrors.INTERNAL_ERROR.getMessage();

    private final String serviceCode;
    private final String rawServiceCode;

    /** 创建使用指定服务标识的错误解析器。 */
    public ErrorResolver(final String serviceCode) {
        final String actualServiceCode =
                Objects.requireNonNull(serviceCode, "serviceCode").trim();
        this.rawServiceCode = actualServiceCode;
        this.serviceCode = actualServiceCode.toUpperCase(Locale.ROOT);
    }

    /** 将错误标识格式化为稳定字符串错误码；服务边界输出使用 {@link #resolveBoundary(ErrorCodeException)}。 */
    public String resolveCode(final ErrorCode errorCode) {
        Objects.requireNonNull(errorCode, "errorCode");
        if (errorCode instanceof ErrorCodeCarrier carrier) {
            return carrier.code();
        }
        if (!(errorCode instanceof Enum<?> errorEnum)) {
            throw new ErrorCodeException(com.kjs.wuli3.core.error.builtin.ErrorFrameworkErrors.INVALID_ERROR_CODE);
        }
        final ErrorModule module = ErrorMetadataResolver.instance().getErrorModule(errorCode);
        final String moduleName = module.name().trim();
        if (moduleName.isBlank()) {
            throw new ErrorCodeException(com.kjs.wuli3.core.error.builtin.ErrorFrameworkErrors.MODULE_NOT_FOUND);
        }
        final String prefix = this.serviceCode.isBlank() ? "" : this.serviceCode + ".";
        return (prefix + moduleName + "." + errorEnum.name()).toUpperCase(Locale.ROOT);
    }

    /** 按错误可见性生成跨服务传播值；远程错误的原始诊断信息保持不变。 */
    public ErrorCodeCarrier resolveBoundary(final ErrorCodeException exception) {
        final ErrorCodeException actualException = Objects.requireNonNull(exception, "exception");
        final ErrorCodeCarrier remote = actualException.asRemoteError().orElse(null);
        final String originalCode =
                remote == null ? this.resolveCode(actualException.getErrorCode()) : remote.originalCode();
        final String currentCode = remote == null ? this.resolveCode(actualException.getErrorCode()) : remote.code();
        final String message = Objects.requireNonNullElse(
                actualException.getMessage(), actualException.getErrorCode().getMessage());
        final ErrorVisibility visibility = actualException.getVisibility();
        final String fallbackCode = this.resolveCode(SystemErrors.INTERNAL_ERROR);
        final String visibleCode =
                switch (visibility) {
                    case PUBLIC, CODE_ONLY -> currentCode;
                    case MESSAGE_ONLY, INTERNAL -> fallbackCode;
                };
        final String visibleMessage =
                switch (visibility) {
                    case PUBLIC, MESSAGE_ONLY -> message;
                    case CODE_ONLY, INTERNAL -> INTERNAL_MESSAGE;
                };
        final ErrorOrigin origin = remote == null ? actualException.getOrigin() : remote.origin();
        final ErrorSeverity severity = remote == null ? actualException.getSeverity() : remote.severity();
        final String sourceService = remote == null ? this.rawServiceCode : remote.sourceService();
        return new ErrorCodeCarrier(originalCode, visibleCode, visibleMessage, origin, severity, sourceService);
    }
}

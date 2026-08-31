package com.kjs.wuli3.core.error.resolver;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.core.error.ErrorPropagationProtocol;
import com.kjs.wuli3.core.error.builtin.ErrorFrameworkErrors;
import java.util.Locale;
import java.util.Objects;

/**
 * 将本地错误声明格式化为稳定的 {@code SERVICE.MODULE.ERROR_NAME} 字符串。
 *
 * 注意：传播错误已经包含完整字符串错误码，解析时必须原样返回，不能重复添加当前服务前缀。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
public final class DefaultErrorCodeResolver implements ErrorCodeResolver {
    private final String serviceCode;

    /**
     * 创建使用指定服务标识作为错误码前缀的解析器。
     *
     * @param serviceCode 服务稳定标识；空字符串表示不添加服务前缀
     */
    public DefaultErrorCodeResolver(final String serviceCode) {
        this.serviceCode =
                Objects.requireNonNull(serviceCode, "serviceCode").trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 解析本地枚举错误声明或直接返回传播错误的完整字符串码。
     *
     * @param errorCode 本地枚举或传播错误标识
     * @return 大写的稳定字符串错误码
     * @throws ErrorCodeException 错误声明没有有效的模块元数据时抛出
     */
    @Override
    public String resolve(final ErrorCode errorCode) {
        Objects.requireNonNull(errorCode, "errorCode");
        if (errorCode instanceof ErrorPropagationProtocol protocol) {
            return protocol.code();
        }
        if (!(errorCode instanceof Enum<?> errorEnum)) {
            throw new ErrorCodeException(ErrorFrameworkErrors.INVALID_ERROR_CODE);
        }
        final ErrorModule errorModule = ErrorMetadataResolver.instance().getErrorModule(errorCode);
        final String moduleName = errorModule.name().trim();
        if (moduleName.isBlank()) {
            throw new ErrorCodeException(ErrorFrameworkErrors.MODULE_NOT_FOUND);
        }
        final String prefix = this.serviceCode.isBlank() ? "" : this.serviceCode + ".";
        return (prefix + moduleName + "." + errorEnum.name()).toUpperCase(Locale.ROOT);
    }
}

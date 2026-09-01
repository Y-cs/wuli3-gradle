package com.kjs.wuli3.core.error.propagation;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorVisibility;
import com.kjs.wuli3.core.error.resolver.ErrorCodeResolver;

import java.util.Objects;

/**
 * 在服务边界统一序列化和反序列化错误。
 *
 * @author GuoYang create on 2026/8/28 20:00
 */
public class ErrorCodeCarrierCodec {

    private final ErrorCodeResolver codeResolver;
    private final String sourceService;

    /** 创建默认编解码器。 */
    public ErrorCodeCarrierCodec(final ErrorCodeResolver codeResolver, final String sourceService) {
        this.codeResolver = Objects.requireNonNull(codeResolver, "codeResolver");
        this.sourceService = Objects.requireNonNull(sourceService, "sourceService");
    }

    /** 按边界可见性生成稳定传输值。 */
    public ErrorCodeCarrier encode(final ErrorCodeException exception) {
        Objects.requireNonNull(exception, "exception");
        final ErrorVisibility visibility = exception.getVisibility();
        final String message =
                switch (visibility) {
                    case CODE_ONLY, INTERNAL -> "Internal server error";
                    case MESSAGE_ONLY, PUBLIC ->
                            Objects.requireNonNullElse(
                                    exception.getMessage(), exception.getErrorCode().getMessage());
                };
        final String source = exception
                .asRemoteError()
                .map(ErrorCodeCarrier::sourceService)
                .orElse(this.sourceService);
        return new ErrorCodeCarrier(
                this.codeResolver.resolve(exception.getErrorCode()),
                message,
                exception.getOrigin(),
                exception.getSeverity(),
                source);
    }

    /** 将传输值直接包装为统一异常。 */
    public ErrorCodeException decode(final ErrorCodeCarrier protocol) {
        return new ErrorCodeException(Objects.requireNonNull(protocol, "protocol"));
    }
}

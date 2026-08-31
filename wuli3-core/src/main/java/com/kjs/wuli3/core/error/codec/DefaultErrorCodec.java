package com.kjs.wuli3.core.error.codec;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorPropagationProtocol;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.resolver.ErrorCodeResolver;
import java.util.Objects;

/** 默认错误编解码器。
 *
 * @author GuoYang create on 2026/8/28 20:00
 */
public final class DefaultErrorCodec implements ErrorCodec {

    private final ErrorCodeResolver codeResolver;
    private final String sourceService;

    /** 创建默认编解码器。 */
    public DefaultErrorCodec(final ErrorCodeResolver codeResolver, final String sourceService) {
        this.codeResolver = Objects.requireNonNull(codeResolver, "codeResolver");
        this.sourceService = Objects.requireNonNull(sourceService, "sourceService");
    }

    /** 按边界可见性生成稳定传输值。 */
    @Override
    public ErrorPropagationProtocol serialize(final ErrorCodeException exception, final ErrorVisibility visibility) {
        Objects.requireNonNull(exception, "exception");
        Objects.requireNonNull(visibility, "visibility");
        final String message =
                switch (visibility) {
                    case CODE_ONLY, INTERNAL -> "Internal server error";
                    case MESSAGE_ONLY, PUBLIC ->
                        Objects.requireNonNullElse(
                                exception.getMessage(), exception.getErrorCode().getMessage());
                };
        final String source = exception
                .asRemoteError()
                .map(ErrorPropagationProtocol::sourceService)
                .orElse(this.sourceService);
        return new ErrorPropagationProtocol(
                this.codeResolver.resolve(exception.getErrorCode()),
                message,
                exception.getOrigin(),
                exception.getSeverity(),
                source);
    }

    /** 将传输值直接包装为统一异常。 */
    @Override
    public ErrorCodeException deserialize(final ErrorPropagationProtocol protocol) {
        return new ErrorCodeException(Objects.requireNonNull(protocol, "protocol"));
    }
}

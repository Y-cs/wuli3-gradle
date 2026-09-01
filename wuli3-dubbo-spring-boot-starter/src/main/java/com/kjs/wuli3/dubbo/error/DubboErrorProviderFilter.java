package com.kjs.wuli3.dubbo.error;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorVisibility;
import com.kjs.wuli3.core.error.builtin.SystemErrors;
import com.kjs.wuli3.core.error.propagation.ErrorCodePropagator;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrierCodec;
import com.kjs.wuli3.core.error.resolver.DefaultErrorCodeResolver;
import com.kjs.wuli3.dubbo.autoconfigure.DubboProperties;
import lombok.Setter;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 把 Dubbo provider 的失败结果转换为 core 定义的稳定错误协议。
 *
 * <p>注意：未声明的异常会收敛为内部系统错误，原始业务枚举、异常类型和消息都不会跨服务边界传播。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
@Setter
@Activate(group = CommonConstants.PROVIDER, order = 100)
public final class DubboErrorProviderFilter implements Filter {
    private static final ErrorCodePropagator ERROR_PROPAGATION_ENCODER = new ErrorCodePropagator();

    /** 按 provider application 隔离并复用线程安全的错误编解码器。 */
    private final ConcurrentMap<String, ErrorCodeCarrierCodec> errorCodecs = new ConcurrentHashMap<>();

    /**
     * 接收 Spring 容器中的 Dubbo 错误传播配置。
     */
    private @Nullable DubboProperties dubboProperties;

    /**
     * 转换同步抛错、同步失败结果以及异步失败结果。
     */
    @Override
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        final DubboProperties properties = this.dubboProperties;
        if (properties == null || !properties.getError().isEnabled()) {
            return invoker.invoke(invocation);
        }
        final Result result;
        final String sourceService = DubboErrorProviderFilter.sourceService(invoker);
        try {
            result = invoker.invoke(invocation);
        } catch (final RuntimeException exception) {
            final AppResponse response = new AppResponse(invocation);
            this.translate(response, exception, sourceService);
            return AsyncRpcResult.newDefaultAsyncResult(response, invocation);
        }
        if (result instanceof AsyncRpcResult asyncResult) {
            asyncResult.whenCompleteWithContext((response, failure) -> {
                if (response != null && response.getException() != null) {
                    this.translate(response, response.getException(), sourceService);
                }
            });
        } else if (result.hasException()) {
            this.translate(result, result.getException(), sourceService);
        }
        return result;
    }

    /**
     * 写入稳定错误协议，并用不包含提供方类型的占位异常替换原始异常。
     */
    private void translate(final Result result, final Throwable exception, final String sourceService) {
        final ErrorCodeException local = exception instanceof ErrorCodeException errorCodeException
                ? errorCodeException
                : new ErrorCodeException(SystemErrors.INTERNAL_ERROR, exception)
                        .withVisibility(ErrorVisibility.INTERNAL);
        final ErrorCodeCarrierCodec errorCodeCarrierCodec = this.errorCodecs.computeIfAbsent(
                sourceService, (final String key) -> new ErrorCodeCarrierCodec(new DefaultErrorCodeResolver(key), key));
        final ErrorCodeCarrier protocol = errorCodeCarrierCodec.encode(local);
        DubboErrorProviderFilter.ERROR_PROPAGATION_ENCODER.inject(protocol, result::setAttachment);
        result.setException(new RuntimeException("Remote service invocation failed"));
    }

    /**
     * 从 Dubbo provider URL 读取 application 标识。
     */
    private static String sourceService(final Invoker<?> invoker) {
        return Objects.requireNonNullElse(invoker.getUrl().getParameter(CommonConstants.APPLICATION_KEY), "");
    }
}

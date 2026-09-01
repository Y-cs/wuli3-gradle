package com.kjs.wuli3.dubbo.error;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.propagation.ErrorCodePropagator;
import com.kjs.wuli3.dubbo.autoconfigure.DubboProperties;
import lombok.Setter;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.jspecify.annotations.Nullable;

/**
 * 把 Dubbo 响应附件中的稳定错误协议还原为唯一的 {@link ErrorCodeException}。
 *
 * <p>注意：错误附件缺失或非法时保留 Dubbo 原始异常，不根据不完整数据推断业务错误。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
@Setter
@Activate(group = CommonConstants.CONSUMER, order = 100)
public final class DubboErrorConsumerFilter implements Filter {
    private static final ErrorCodePropagator ERROR_PROPAGATION_ENCODER = new ErrorCodePropagator();

    /**
     * 接收 Spring 容器中的 Dubbo 错误传播配置。
     */
    private @Nullable DubboProperties dubboProperties;

    /**
     * 在同步或异步响应完成后尝试恢复协议无关错误。
     */
    @Override
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        final DubboProperties properties = this.dubboProperties;
        if (properties == null || !properties.getError().isEnabled()) {
            return invoker.invoke(invocation);
        }
        final Result result = invoker.invoke(invocation);
        if (result instanceof AsyncRpcResult asyncResult) {
            asyncResult.whenCompleteWithContext((response, failure) -> {
                if (response != null) {
                    DubboErrorConsumerFilter.translate(response);
                }
            });
        } else {
            DubboErrorConsumerFilter.translate(result);
        }
        return result;
    }

    /**
     * 使用完整且合法的错误附件替换 Dubbo 占位异常。
     */
    private static void translate(final Result result) {
        DubboErrorConsumerFilter.ERROR_PROPAGATION_ENCODER
                .extract(result::getAttachment)
                .ifPresent(protocol -> {
                    final Throwable remoteFailure = result.getException();
                    result.setException(new ErrorCodeException(protocol, remoteFailure));
                });
    }
}

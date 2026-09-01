package com.kjs.wuli3.dubbo.context;

import com.kjs.wuli3.dubbo.autoconfigure.DubboProperties;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.store.ContextReader;
import lombok.Setter;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.jspecify.annotations.Nullable;

/**
 * 在 Dubbo consumer 发起调用前把当前 Wuli3 上下文写入 invocation attachments。
 *
 * 注意：依赖由 Dubbo SpringExtensionFactory 通过 setter 注入；非 Spring 环境或功能关闭时保持原调用行为。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
@Setter
@Activate(group = CommonConstants.CONSUMER, order = -100)
public final class DubboContextConsumerFilter implements Filter {
    /**
     * -- SETTER --
     * 接收 Spring 容器中的 Dubbo 传播配置。
     */
    private @Nullable DubboProperties dubboProperties;
    /**
     * -- SETTER --
     * 接收 Spring 容器中可由应用覆盖的上下文编码器。
     */
    private @Nullable ContextPropagator contextPropagator;
    /**
     * -- SETTER --
     * 接收用于捕获当前线程上下文的读取器。
     */
    private @Nullable ContextReader contextReader;

    /** 在调用 provider 前捕获当前上下文并写入 Dubbo 请求附件。 */
    @Override
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        final DubboProperties properties = this.dubboProperties;
        final ContextPropagator encoder = this.contextPropagator;
        final ContextReader reader = this.contextReader;
        if (properties == null || !properties.getContext().isEnabled() || encoder == null || reader == null) {
            return invoker.invoke(invocation);
        }
        encoder.inject(reader.capture(), invocation::setAttachment);
        return invoker.invoke(invocation);
    }
}

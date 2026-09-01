package com.kjs.wuli3.dubbo.context;

import com.kjs.wuli3.dubbo.autoconfigure.DubboProperties;
import com.kjs.wuli3.propagation.ContextProxy;
import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
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
 * 在 Dubbo provider 调用服务方法期间恢复请求 attachments 中的 Wuli3 上下文。
 *
 * 注意：作用域只覆盖当前调用线程，业务自行创建的异步任务仍需显式传播上下文。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
@Setter
@Activate(group = CommonConstants.PROVIDER, order = -100)
public final class DubboContextProviderFilter implements Filter {
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
     * 接收负责恢复上下文并生成线程绑定作用域的传播器。
     */
    private @Nullable ContextProxy contextProxy;

    /** 在服务方法调用期间恢复远程上下文，并在当前线程中可靠关闭作用域。 */
    @Override
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        final DubboProperties properties = this.dubboProperties;
        final ContextPropagator encoder = this.contextPropagator;
        final ContextProxy propagator = this.contextProxy;
        if (properties == null || !properties.getContext().isEnabled() || encoder == null || propagator == null) {
            return invoker.invoke(invocation);
        }
        try (ContextScope scope = propagator.restore(encoder.extract(invocation::getAttachment))) {
            return invoker.invoke(invocation);
        }
    }
}

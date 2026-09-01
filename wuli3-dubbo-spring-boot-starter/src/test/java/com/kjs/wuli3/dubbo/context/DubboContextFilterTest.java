package com.kjs.wuli3.dubbo.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.dubbo.autoconfigure.DubboProperties;
import com.kjs.wuli3.propagation.DefaultContextProxy;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.codec.InvocationContextCodec;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextStore;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.junit.jupiter.api.Test;

/**
 * 验证 Dubbo consumer/provider Filter 的上下文附件传播和作用域恢复。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
class DubboContextFilterTest {
    private final ContextPropagator encoder = new ContextPropagator(ContextPropagator.standardContextEncoder());
    private final DubboProperties properties = new DubboProperties();

    /** 验证 consumer 会在调用前把当前线程上下文写入请求附件。 */
    @Test
    void consumerWritesCurrentContextToInvocationAttachments() {
        final ContextStore store = new ContextStore();
        store.put(new InvocationContext("10.0.0.8", "request-42"));
        final Invocation invocation = mock(Invocation.class);
        final Invoker<?> invoker = mock(Invoker.class);
        when(invoker.invoke(invocation)).thenReturn(new AppResponse());
        final DubboContextConsumerFilter filter = new DubboContextConsumerFilter();
        filter.setDubboProperties(this.properties);
        filter.setContextPropagator(this.encoder);
        filter.setContextReader(store);

        filter.invoke(invoker, invocation);

        verify(invocation).setAttachment(InvocationContextCodec.REQUEST_ID, "request-42");
        verify(invocation).setAttachment(InvocationContextCodec.ORIGIN_IP, "10.0.0.8");
    }

    /** 验证 provider 只在服务调用作用域内使用远程上下文，结束后恢复原上下文。 */
    @Test
    void providerRestoresRemoteContextOnlyForInvocationScope() {
        final ContextStore store = new ContextStore();
        store.put(new InvocationContext("127.0.0.1", "previous"));
        final Invocation invocation = mock(Invocation.class);
        when(invocation.getAttachment(InvocationContextCodec.REQUEST_ID)).thenReturn("request-42");
        when(invocation.getAttachment(InvocationContextCodec.ORIGIN_IP)).thenReturn("10.0.0.8");
        final Invoker<?> invoker = mock(Invoker.class);
        when(invoker.invoke(invocation)).thenAnswer(ignored -> {
            assertThat(store.get(InvocationContext.class)).contains(new InvocationContext("10.0.0.8", "request-42"));
            return new AppResponse();
        });
        final DubboContextProviderFilter filter = new DubboContextProviderFilter();
        filter.setDubboProperties(this.properties);
        filter.setContextPropagator(this.encoder);
        filter.setContextProxy(new DefaultContextProxy(store));

        filter.invoke(invoker, invocation);

        assertThat(store.get(InvocationContext.class)).contains(new InvocationContext("127.0.0.1", "previous"));
    }
}

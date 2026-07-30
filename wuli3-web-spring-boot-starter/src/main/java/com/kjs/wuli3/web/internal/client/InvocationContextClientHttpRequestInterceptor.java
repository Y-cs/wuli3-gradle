package com.kjs.wuli3.web.internal.client;

import com.kjs.wuli3.propagation.codec.AuthContextCodec;
import com.kjs.wuli3.propagation.codec.DefaultPropagationContextCodecs;
import com.kjs.wuli3.propagation.codec.InvocationContextCodec;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.propagation.transmission.ContextTransmitter;
import java.io.IOException;
import java.util.Objects;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 将当前调用链上下文写入 HTTP 出站请求。
 *
 * <p>该拦截器始终使用 {@code invocationOnly()}，不会通过 HTTP 自动传播认证上下文。
 */
public final class InvocationContextClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final ContextTransmitter transmitter;

    public InvocationContextClientHttpRequestInterceptor(final ContextStore contextStore) {
        final ContextStore store = Objects.requireNonNull(contextStore, "contextStore");
        this.transmitter = new ContextTransmitter(store, store, DefaultPropagationContextCodecs.invocationOnly());
    }

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
            throws IOException {
        request.getHeaders().remove(InvocationContextCodec.REQUEST_ID);
        request.getHeaders().remove(InvocationContextCodec.ORIGIN_IP);
        request.getHeaders().remove(AuthContextCodec.USER_ID);
        request.getHeaders().remove(AuthContextCodec.USERNAME);
        this.transmitter.writeTo(request.getHeaders()::set);
        return execution.execute(request, body);
    }
}

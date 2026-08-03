package com.kjs.wuli3.web.internal.client;

import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.encoding.AuthContextEncoder;
import com.kjs.wuli3.propagation.encoding.InvocationContextEncoder;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.io.IOException;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 将当前调用链上下文写入 HTTP 出站请求。
 *
 * <p>该拦截器只写入调用链上下文，不会通过 HTTP 自动传播认证上下文。
 */
public final class InvocationContextClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final ContextReader contextReader;

    public InvocationContextClientHttpRequestInterceptor(final ContextReader contextReader) {
        this.contextReader = Objects.requireNonNull(contextReader, "contextReader");
    }

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
            throws IOException {
        final HttpHeaders headers = request.getHeaders();
        headers.remove(InvocationContextEncoder.REQUEST_ID);
        headers.remove(InvocationContextEncoder.ORIGIN_IP);
        headers.remove(AuthContextEncoder.USER_ID);
        headers.remove(AuthContextEncoder.USERNAME);
        final ContextSnapshot snapshot = this.contextReader.capture();
        snapshot.get(InvocationContext.class)
                .ifPresent(context -> InvocationContextEncoder.writeTo(context, headers::set));
        return execution.execute(request, body);
    }
}

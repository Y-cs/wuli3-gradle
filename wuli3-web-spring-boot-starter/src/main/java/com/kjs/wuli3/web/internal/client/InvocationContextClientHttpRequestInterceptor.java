package com.kjs.wuli3.web.internal.client;

import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.store.ContextReader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Objects;

/**
 * 将当前调用链上下文写入 HTTP 出站请求。
 *
 * <p>该拦截器只写入调用链上下文，不会通过 HTTP 自动传播认证上下文。
 */
public final class InvocationContextClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final ContextReader contextReader;
    private final ContextEncoder contextEncoder;

    public InvocationContextClientHttpRequestInterceptor(final ContextReader contextReader) {
        this.contextReader = Objects.requireNonNull(contextReader, "contextReader");
        this.contextEncoder = new ContextEncoder(ContextEncoder.standardContextEncoder());
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        final HttpHeaders headers = request.getHeaders();
        this.contextEncoder.reservedFieldNames()
                .forEach(headers::remove);
        this.contextEncoder.writeTo(this.contextReader.capture(), headers::set);
        return execution.execute(request, body);
    }
}

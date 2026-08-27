package com.kjs.wuli3.web.internal.client;

import com.kjs.wuli3.propagation.encoding.ContextEncoder;
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
 * <p>实际传播范围由注入的 {@link ContextEncoder} 决定；默认自动配置会同时传播调用和认证上下文，目标端必须属于可信边界。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class InvocationContextClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final ContextReader contextReader;
    private final ContextEncoder contextEncoder;

    public InvocationContextClientHttpRequestInterceptor(
            final ContextReader contextReader, final ContextEncoder contextEncoder) {
        this.contextReader = Objects.requireNonNull(contextReader, "contextReader");
        this.contextEncoder = Objects.requireNonNull(contextEncoder, "contextEncoder");
    }

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
            throws IOException {
        final HttpHeaders headers = request.getHeaders();
        this.contextEncoder.reservedFieldNames().forEach(headers::remove);
        this.contextEncoder.writeTo(this.contextReader.capture(), headers::set);
        return execution.execute(request, body);
    }
}

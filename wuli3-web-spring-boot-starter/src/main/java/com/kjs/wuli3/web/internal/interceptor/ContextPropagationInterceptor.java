package com.kjs.wuli3.web.internal.interceptor;

import com.kjs.wuli3.propagation.codec.ContextPropagator;
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
 * <p>实际传播范围由注入的 {@link ContextPropagator} 决定；默认自动配置会同时传播调用和认证上下文，目标端必须属于可信边界。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ContextPropagationInterceptor implements ClientHttpRequestInterceptor {

    private final ContextReader contextReader;
    private final ContextPropagator contextPropagator;

    public ContextPropagationInterceptor(
            final ContextReader contextReader, final ContextPropagator contextPropagator) {
        this.contextReader = Objects.requireNonNull(contextReader, "contextReader");
        this.contextPropagator = Objects.requireNonNull(contextPropagator, "contextPropagator");
    }

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
            throws IOException {
        final HttpHeaders headers = request.getHeaders();
        this.contextPropagator.reservedFieldNames().forEach(headers::remove);
        this.contextPropagator.inject(this.contextReader.capture(), headers::set);
        return execution.execute(request, body);
    }
}

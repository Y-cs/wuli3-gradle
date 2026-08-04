package com.kjs.wuli3.web.internal.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.encoding.AuthContextEncoder;
import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.encoding.InvocationContextEncoder;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class InvocationContextClientHttpRequestInterceptorTest {

    @Test
    void rebuildsStandardPropagationHeadersFromTheCurrentContext() throws Exception {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
        contextStore.put(new AuthContext(7L, "alice"));
        final InvocationContextClientHttpRequestInterceptor interceptor =
                new InvocationContextClientHttpRequestInterceptor(
                        contextStore, new ContextEncoder(ContextEncoder.standardContextEncoder()));
        final HttpHeaders headers = new HttpHeaders();
        headers.set(InvocationContextEncoder.REQUEST_ID, "forged-request");
        headers.set(InvocationContextEncoder.ORIGIN_IP, "203.0.113.8");
        headers.set(AuthContextEncoder.USER_ID, "99");
        headers.set(AuthContextEncoder.USERNAME, "mallory");
        final HttpRequest request = mock(HttpRequest.class);
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final byte[] body = new byte[0];
        when(request.getHeaders()).thenReturn(headers);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://service.example/orders"));
        when(execution.execute(request, body)).thenReturn(response);

        assertThat(interceptor.intercept(request, body, execution)).isSameAs(response);

        assertThat(headers.getFirst(InvocationContextEncoder.REQUEST_ID)).isEqualTo("request-42");
        assertThat(headers.getFirst(InvocationContextEncoder.ORIGIN_IP)).isEqualTo("10.0.0.8");
        assertThat(headers.getFirst(AuthContextEncoder.USER_ID)).isEqualTo("7");
        assertThat(headers.getFirst(AuthContextEncoder.USERNAME)).isEqualTo("alice");
        verify(execution).execute(request, body);
    }

    @Test
    void removesReservedHeadersWhenNoContextIsAvailable() throws Exception {
        final ContextStore contextStore = new ContextStore();
        final InvocationContextClientHttpRequestInterceptor interceptor =
                new InvocationContextClientHttpRequestInterceptor(
                        contextStore, new ContextEncoder(ContextEncoder.standardContextEncoder()));
        final HttpHeaders headers = new HttpHeaders();
        headers.set(InvocationContextEncoder.REQUEST_ID, "forged-request");
        headers.set(InvocationContextEncoder.ORIGIN_IP, "203.0.113.8");
        headers.set(AuthContextEncoder.USER_ID, "99");
        headers.set(AuthContextEncoder.USERNAME, "mallory");
        final HttpRequest request = mock(HttpRequest.class);
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final byte[] body = new byte[0];
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, body)).thenReturn(response);

        assertThat(interceptor.intercept(request, body, execution)).isSameAs(response);

        assertThat(headers)
                .doesNotContainKey(InvocationContextEncoder.REQUEST_ID)
                .doesNotContainKey(InvocationContextEncoder.ORIGIN_IP)
                .doesNotContainKey(AuthContextEncoder.USER_ID)
                .doesNotContainKey(AuthContextEncoder.USERNAME);
    }
}

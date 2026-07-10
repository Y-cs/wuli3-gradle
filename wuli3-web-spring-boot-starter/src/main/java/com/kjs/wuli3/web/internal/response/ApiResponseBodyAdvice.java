package com.kjs.wuli3.web.internal.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.json.core.JsonErrors;
import com.kjs.wuli3.web.config.properties.WebResponseProperties;
import com.kjs.wuli3.web.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 将普通返回值统一包装成 {@link ApiResponse}。
 */
@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ApiResponseFactory responseFactory;
    private final WebResponseProperties responseProperties;
    private final ObjectMapper objectMapper;

    public ApiResponseBodyAdvice(
            final ApiResponseFactory responseFactory,
            final WebResponseProperties responseProperties,
            final ObjectMapper objectMapper) {
        this.responseFactory = responseFactory;
        this.responseProperties = responseProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(
            final MethodParameter returnType, final Class<? extends HttpMessageConverter<?>> converterType) {
        // NativeResponse 的默认语义是跳过成功响应包装，异常仍交给 WebExceptionHandler。
        return !NativeResponseSupport.hasNativeResponse(returnType);
    }

    @Override
    public @Nullable Object beforeBodyWrite(
            final @Nullable Object body,
            final MethodParameter returnType,
            final MediaType selectedContentType,
            final Class<? extends HttpMessageConverter<?>> selectedConverterType,
            final ServerHttpRequest request,
            final ServerHttpResponse response) {
        if (ApiResponseBodyAdvice.shouldSkip(response)) {
            return body;
        }
        if (!this.responseProperties.isWrapResponseEntityBody()
                && returnType.getParameterType() == ResponseEntity.class) {
            return body;
        }
        if (body instanceof ApiResponse<?> || body instanceof ProblemDetail) {
            return body;
        }
        // 二进制、资源和流式返回值必须保持原始 body，避免破坏下载和长连接响应。
        if (body instanceof byte[]
                || body instanceof Resource
                || body instanceof ResponseBodyEmitter
                || body instanceof StreamingResponseBody) {
            return body;
        }
        // String 返回值默认由 StringHttpMessageConverter 处理，需要提前序列化为 JSON 字符串。
        if (body instanceof String || StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return this.toJson(this.responseFactory.success(body));
        }
        return this.responseFactory.success(body);
    }

    private String toJson(final Object body) {
        try {
            return this.objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new ErrorCodeException(JsonErrors.SERIALIZATION_FAILED, ex);
        }
    }

    private static boolean shouldSkip(final ServerHttpResponse response) {
        if (!(response instanceof ServletServerHttpResponse servletResponse)) {
            return false;
        }
        final HttpServletResponse rawResponse = servletResponse.getServletResponse();
        final int status = rawResponse.getStatus();
        // 204/304 按 HTTP 语义不能携带响应体。
        return status == HttpServletResponse.SC_NO_CONTENT || status == HttpServletResponse.SC_NOT_MODIFIED;
    }
}

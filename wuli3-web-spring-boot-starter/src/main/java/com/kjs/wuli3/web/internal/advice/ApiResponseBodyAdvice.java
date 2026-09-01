package com.kjs.wuli3.web.internal.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.json.core.JsonErrors;
import com.kjs.wuli3.web.response.ApiResponse;
import com.kjs.wuli3.web.response.WebResponseProperties;
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
 *
 * <p>本类是成功响应统一包装的核心实现，处理 Controller 返回值并将其包装为标准的 {@link ApiResponse} 结构。
 *
 * <h2>包装规则</h2>
 * <ul>
 *   <li>普通对象、集合、{@code null}：包装为 {@code ApiResponse.success(data)}</li>
 *   <li>{@code String} 返回值：特殊处理，提前序列化为 JSON 字符串并设置 {@code Content-Type: application/json}</li>
 *   <li>已返回 {@code ApiResponse<?>}、{@code ProblemDetail}：原样返回，不重复包装</li>
 *   <li>二进制、资源、流式：{@code byte[]}、{@code Resource}、{@code ResponseBodyEmitter}、
 *       {@code StreamingResponseBody} 原样返回</li>
 *   <li>HTTP 204/304：跳过 body 写入</li>
 *   <li>{@code ResponseEntity<T>}：根据 {@code wrap-response-entity-body} 配置决定是否包装 body</li>
 * </ul>
 *
 * <h2>String 返回值特殊处理</h2>
 * <p>{@code String} 返回值需要提前序列化的原因：Spring MVC 的 {@link StringHttpMessageConverter} 会直接将
 * {@code String} 写入响应流，不经过 JSON 序列化。如果返回 {@code ApiResponse} 对象而不序列化，
 * {@code StringHttpMessageConverter} 会调用 {@code toString()} 而不是 JSON 序列化，导致响应格式错误。
 * 因此对于 {@code String} 返回值，必须：
 * <ol>
 *   <li>将 {@code ApiResponse} 提前序列化为 JSON 字符串</li>
 *   <li>显式设置 {@code Content-Type: application/json}</li>
 *   <li>返回序列化后的 JSON 字符串，让 {@code StringHttpMessageConverter} 直接写出</li>
 * </ol>
 *
 * <h2>@NativeResponse 支持</h2>
 * <p>标注 {@code @NativeResponse} 的方法会跳过本 advice，直接返回原始响应。
 * 异常响应仍由 {@link com.kjs.wuli3.web.internal.handler.WebExceptionHandler} 处理。
 *
 * @author GuoYang create on 2026/8/17 11:53
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

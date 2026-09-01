package com.kjs.wuli3.web.internal.handler;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.web.error.WebErrorStatusResolver;
import com.kjs.wuli3.web.error.WebErrors;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Spring MVC 与 wuli 错误的默认 HTTP 状态映射。
 *
 * <p>本实现基于错误元数据（特别是 {@link ErrorOrigin}）将异常映射为合适的 HTTP 状态码：
 * <ul>
 *   <li>安全异常：AuthenticationException → 401, AccessDeniedException → 403</li>
 *   <li>业务错误（{@link ErrorCodeException}）：
 *     <ul>
 *       <li>{@link ErrorOrigin#SERVER} → 500 Internal Server Error（服务端责任）</li>
 *       <li>{@link ErrorOrigin#CALLER} → 400 Bad Request（调用方责任）</li>
 *     </ul>
 *   </li>
 *   <li>框架异常：404, 405, 415, 413 等根据异常类型映射</li>
 *   <li>未知异常：500 Internal Server Error</li>
 * </ul>
 *
 * <p>ErrorOrigin 元数据通过 {@link ErrorCodeException#getOrigin()} 获取，该方法会：
 * <ol>
 *   <li>对于本地错误枚举：从 {@code @ErrorMetadata} 注解中解析</li>
 *   <li>对于远程传播错误（{@link com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier}）：
 *       使用传播时携带的 origin 值，保持跨服务边界的责任归属一致性</li>
 * </ol>
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DefaultWebErrorStatusResolver implements WebErrorStatusResolver {

    @Override
    public HttpStatus resolve(final Throwable error, final ErrorCode responseCode) {
        final HttpStatus securityStatus = DefaultWebErrorStatusResolver.securityStatus(error);
        if (securityStatus != null) {
            return securityStatus;
        }
        if (responseCode == WebErrors.PAYLOAD_TOO_LARGE) {
            return HttpStatus.PAYLOAD_TOO_LARGE;
        }
        switch (error) {
            case ErrorCodeException errorCodeException -> {
                return DefaultWebErrorStatusResolver.status(errorCodeException.getOrigin());
            }
            case ErrorResponseException errorResponseException -> {
                return HttpStatus.valueOf(errorResponseException.getStatusCode().value());
            }
            default -> {}
        }
        if (error instanceof NoHandlerFoundException || error instanceof NoResourceFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        switch (error) {
            case HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException -> {
                return HttpStatus.METHOD_NOT_ALLOWED;
            }
            case HttpMediaTypeNotSupportedException httpMediaTypeNotSupportedException -> {
                return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            }
            case HttpMessageNotWritableException httpMessageNotWritableException -> {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
            default -> {}
        }
        if (DefaultWebErrorStatusResolver.badRequest(error)) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static boolean badRequest(final Throwable error) {
        return error instanceof MethodArgumentNotValidException
                || error instanceof MethodArgumentTypeMismatchException
                || error instanceof ServletRequestBindingException
                || error instanceof HttpMessageNotReadableException
                || error instanceof ConstraintViolationException;
    }

    /**
     * 根据错误责任归属映射 HTTP 状态码。
     *
     * <p>SERVER 责任表示服务端内部问题（500），CALLER 责任表示调用方输入或使用错误（400）。
     *
     * @param origin 错误责任归属（来自 @ErrorMetadata 或传播协议）
     * @return 对应的 HTTP 状态码
     */
    private static HttpStatus status(final ErrorOrigin origin) {
        return origin == ErrorOrigin.SERVER ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
    }

    private static @Nullable HttpStatus securityStatus(final Throwable error) {
        if (DefaultWebErrorStatusResolver.hasCause(
                error, "org.springframework.security.core.AuthenticationException")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (DefaultWebErrorStatusResolver.hasCause(
                error, "org.springframework.security.access.AccessDeniedException")) {
            return HttpStatus.FORBIDDEN;
        }
        return null;
    }

    private static boolean hasCause(final Throwable error, final String className) {
        Throwable current = error;
        while (current != null) {
            Class<?> type = current.getClass();
            while (type != null) {
                if (className.equals(type.getName())) {
                    return true;
                }
                type = type.getSuperclass();
            }
            current = current.getCause();
        }
        return false;
    }
}

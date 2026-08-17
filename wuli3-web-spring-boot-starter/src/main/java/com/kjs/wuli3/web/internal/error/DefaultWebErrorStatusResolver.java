package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
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

/** Spring MVC 与 wuli 错误的默认 HTTP 状态映射。
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
                return DefaultWebErrorStatusResolver.status(
                        errorCodeException.getResolvedErrorPolicy().origin());
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

    private static HttpStatus status(final ErrorOrigin origin) {
        return origin == ErrorOrigin.SYSTEM ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
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

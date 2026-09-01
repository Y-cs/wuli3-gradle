package com.kjs.wuli3.web.internal.advice;

import com.kjs.wuli3.web.response.NativeResponse;
import com.kjs.wuli3.web.response.NativeResponseMode;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 解析控制器方法和请求上的原生响应模式。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class NativeResponseSupport {

    private NativeResponseSupport() {}

    /** 判断返回类型是否声明了原生响应模式。 */
    public static boolean hasNativeResponse(final MethodParameter returnType) {
        return NativeResponseSupport.resolveMode(returnType) != null;
    }

    /** 判断当前请求是否要求所有响应保持原生形式。 */
    public static boolean isAll(final HttpServletRequest request) {
        return NativeResponseSupport.resolveMode(request) == NativeResponseMode.ALL;
    }

    /** 从返回类型的方法或所属类解析原生响应模式。 */
    public static @Nullable NativeResponseMode resolveMode(final MethodParameter returnType) {
        final NativeResponse nativeResponse = NativeResponseSupport.findNativeResponse(returnType);
        return nativeResponse == null ? null : nativeResponse.value();
    }

    /** 从当前请求匹配的处理器解析原生响应模式。 */
    public static @Nullable NativeResponseMode resolveMode(final HttpServletRequest request) {
        // 异常处理方法的 MethodParameter 属于 ControllerAdvice，这里要回到原始 handler 查注解。
        final Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod handlerMethod) {
            final NativeResponse nativeResponse = NativeResponseSupport.findNativeResponse(handlerMethod);
            return nativeResponse == null ? null : nativeResponse.value();
        }
        return null;
    }

    private static @Nullable NativeResponse findNativeResponse(final MethodParameter returnType) {
        if (returnType.getMethod() != null) {
            final NativeResponse nativeResponse =
                    AnnotatedElementUtils.findMergedAnnotation(returnType.getMethod(), NativeResponse.class);
            if (nativeResponse != null) {
                return nativeResponse;
            }
        }
        return AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(), NativeResponse.class);
    }

    private static @Nullable NativeResponse findNativeResponse(final HandlerMethod handlerMethod) {
        final NativeResponse nativeResponse =
                AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), NativeResponse.class);
        if (nativeResponse != null) {
            return nativeResponse;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), NativeResponse.class);
    }
}

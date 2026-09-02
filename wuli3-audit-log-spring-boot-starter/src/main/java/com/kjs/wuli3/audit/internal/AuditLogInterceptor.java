package com.kjs.wuli3.audit.internal;

import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.payload.AuditLog;
import java.lang.reflect.Method;
import java.util.Objects;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.expression.EvaluationContext;

/**
 * 拦截标记 {@link com.kjs.wuli3.audit.annotation.AuditLog} 注解的方法，根据注解配置自动记录审计日志。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class AuditLogInterceptor implements MethodInterceptor {

    private final AuditLogRecorder auditLogRecorder;
    private final AuditLogExpressionEvaluator evaluator;

    /**
     * 创建审计日志拦截器。
     *
     * @param auditLogRecorder 审计日志记录器
     */
    public AuditLogInterceptor(final AuditLogRecorder auditLogRecorder) {
        this.auditLogRecorder = Objects.requireNonNull(auditLogRecorder, "auditLogRecorder");
        this.evaluator = new AuditLogExpressionEvaluator();
    }

    @Override
    public @Nullable Object invoke(final MethodInvocation invocation) throws Throwable {
        final Method method = AuditLogInterceptor.targetMethod(invocation);
        final com.kjs.wuli3.audit.annotation.AuditLog annotation =
                method.getAnnotation(com.kjs.wuli3.audit.annotation.AuditLog.class);
        if (annotation == null) {
            return invocation.proceed();
        }
        try {
            final Object result = invocation.proceed();
            this.recordSuccess(annotation, method, invocation.getArguments(), result);
            return result;
        } catch (final Throwable ex) {
            if (annotation.recordFailure()) {
                this.recordFailure(annotation, method, invocation.getArguments(), ex);
            }
            throw ex;
        }
    }

    /**
     * 解析出声明注解的实现方法。
     *
     * <p>基于接口的 JDK 代理只能拿到接口方法，注解通常声明在实现类上，因此需要回落到最具体的方法。
     */
    private static Method targetMethod(final MethodInvocation invocation) {
        final Object target = invocation.getThis();
        if (target == null) {
            return invocation.getMethod();
        }
        return AopUtils.getMostSpecificMethod(invocation.getMethod(), AopProxyUtils.ultimateTargetClass(target));
    }

    private void recordSuccess(
            final com.kjs.wuli3.audit.annotation.AuditLog annotation,
            final Method method,
            final Object[] args,
            final @Nullable Object result) {
        final AuditLog entry =
                this.buildEntry(annotation, method, args, result, null, AuditLog.AuditLogOutcome.SUCCESS);
        this.auditLogRecorder.record(entry, annotation.afterCommit());
    }

    private void recordFailure(
            final com.kjs.wuli3.audit.annotation.AuditLog annotation,
            final Method method,
            final Object[] args,
            final Throwable exception) {
        final AuditLog entry =
                this.buildEntry(annotation, method, args, null, exception, AuditLog.AuditLogOutcome.FAILURE);
        this.auditLogRecorder.record(entry, annotation.afterCommit());
    }

    private AuditLog buildEntry(
            final com.kjs.wuli3.audit.annotation.AuditLog annotation,
            final Method method,
            final Object[] args,
            final @Nullable Object result,
            final @Nullable Throwable exception,
            final AuditLog.AuditLogOutcome outcome) {
        final EvaluationContext context = this.evaluator.createContext(method, args, result, exception);
        final String targetId = this.evaluator.evaluate(annotation.targetId(), context);
        final String content = this.evaluator.evaluate(annotation.content(), context);
        return new AuditLog(annotation.module(), targetId, annotation.action(), content, outcome);
    }
}

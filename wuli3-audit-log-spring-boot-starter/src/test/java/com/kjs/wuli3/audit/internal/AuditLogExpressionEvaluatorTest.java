package com.kjs.wuli3.audit.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;

/**
 * 验证审计 SpEL 模板缓存不会保留单次调用数据。
 *
 * @author GuoYang create on 2026/9/1 19:01
 */
class AuditLogExpressionEvaluatorTest {

    /** 验证复用已编译模板时仍从当前调用上下文读取变量。 */
    @Test
    void reusesCompiledTemplateWithoutRetainingInvocationValues() throws NoSuchMethodException {
        final AuditLogExpressionEvaluator evaluator = new AuditLogExpressionEvaluator();
        final Method method = AuditLogExpressionEvaluatorTest.SampleService.class.getDeclaredMethod(
                "record", String.class, int.class);

        final EvaluationContext firstContext =
                evaluator.createContext(method, new Object[] {"order-1", 12}, null, null);
        final EvaluationContext secondContext =
                evaluator.createContext(method, new Object[] {"order-2", 35}, null, null);

        final String template = "订单 #{#orderId} 金额 #{#amount}";
        assertThat(evaluator.evaluate(template, firstContext)).isEqualTo("订单 order-1 金额 12");
        assertThat(evaluator.evaluate(template, secondContext)).isEqualTo("订单 order-2 金额 35");
    }

    /** 验证只读上下文拒绝 SpEL 方法调用，避免表达式执行任意对象方法。 */
    @Test
    void rejectsMethodInvocationInSimpleEvaluationContext() throws NoSuchMethodException {
        final AuditLogExpressionEvaluator evaluator = new AuditLogExpressionEvaluator();
        final Method method = AuditLogExpressionEvaluatorTest.SampleService.class.getDeclaredMethod(
                "record", String.class, int.class);
        final EvaluationContext context = evaluator.createContext(method, new Object[] {"order-1", 12}, null, null);

        assertThatThrownBy(() -> evaluator.evaluate("#{#orderId.toUpperCase()}", context))
                .isInstanceOf(EvaluationException.class);
    }

    /** 验证 SpEL 求值失败时仍抛出原始异常，日志仅用于诊断。 */
    @Test
    void rethrowsSpelEvaluationFailure() throws NoSuchMethodException {
        final AuditLogExpressionEvaluator evaluator = new AuditLogExpressionEvaluator();
        final Method method = AuditLogExpressionEvaluatorTest.SampleService.class.getDeclaredMethod(
                "record", String.class, int.class);
        final EvaluationContext context = evaluator.createContext(method, new Object[] {"order-1", 12}, null, null);

        assertThatThrownBy(() -> evaluator.evaluate("#{#missing.value}", context))
                .isInstanceOf(EvaluationException.class);
    }

    private static final class SampleService {

        @SuppressWarnings("UnusedMethod")
        private String record(final String orderId, final int amount) {
            return orderId + amount;
        }
    }
}

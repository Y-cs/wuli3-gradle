package com.kjs.wuli3.audit.internal;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 为审计注解解析并求值 SpEL 模板表达式。
 *
 * <p>模板形如 {@code "创建订单 #{#order.id}"}，其中 {@code #{}} 包裹的是 SpEL 表达式，
 * 模板外的文本原样保留。不含 {@code #{}} 时视为字面量直接返回。
 *
 * <p>可用变量：方法参数（按名称）、{@code #result}（方法返回值）、{@code #exception}（捕获的异常）。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
final class AuditLogExpressionEvaluator {

    private static final String TEMPLATE_PREFIX = "#{";
    private static final String TEMPLATE_SUFFIX = "}";
    private static final String RESULT_VARIABLE = "result";
    private static final String EXCEPTION_VARIABLE = "exception";

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /**
     * 求值模板字符串，将 {@code #{}} 包裹的 SpEL 表达式替换为实际值。
     *
     * @param template 模板字符串
     * @param method 被拦截的方法
     * @param args 方法参数
     * @param result 方法返回值；方法抛出异常时为 {@code null}
     * @param exception 方法抛出的异常；正常返回时为 {@code null}
     * @return 求值后的字符串
     */
    public String evaluate(
            final String template,
            final Method method,
            final Object[] args,
            final @Nullable Object result,
            final @Nullable Throwable exception) {
        if (!template.contains(TEMPLATE_PREFIX)) {
            return template;
        }
        final StandardEvaluationContext context = this.buildContext(method, args, result, exception);
        final StringBuilder sb = new StringBuilder();
        int pos = 0;
        while (true) {
            final int start = template.indexOf(TEMPLATE_PREFIX, pos);
            if (start == -1) {
                sb.append(template.substring(pos));
                break;
            }
            sb.append(template, pos, start);
            final int end = template.indexOf(TEMPLATE_SUFFIX, start + TEMPLATE_PREFIX.length());
            if (end == -1) {
                sb.append(template.substring(start));
                break;
            }
            final String expressionString = template.substring(start + TEMPLATE_PREFIX.length(), end);
            final Expression expression = this.getExpression(expressionString);
            final Object value = expression.getValue(context);
            sb.append(value == null ? "" : value);
            pos = end + TEMPLATE_SUFFIX.length();
        }
        return sb.toString();
    }

    private StandardEvaluationContext buildContext(
            final Method method,
            final Object[] args,
            final @Nullable Object result,
            final @Nullable Throwable exception) {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        final String[] parameterNames = this.parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames != null && args != null) {
            for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        if (result != null) {
            context.setVariable(RESULT_VARIABLE, result);
        }
        if (exception != null) {
            context.setVariable(EXCEPTION_VARIABLE, exception);
        }
        return context;
    }

    private Expression getExpression(final String expressionString) {
        return this.expressionCache.computeIfAbsent(expressionString, this.parser::parseExpression);
    }
}

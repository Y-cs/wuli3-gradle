package com.kjs.wuli3.audit.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

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
public final class AuditLogExpressionEvaluator {

    private static final String TEMPLATE_PREFIX = "#{";
    private static final String TEMPLATE_SUFFIX = "}";
    private static final String RESULT_VARIABLE = "result";
    private static final String EXCEPTION_VARIABLE = "exception";
    private static final long MAX_CACHE_SIZE = 256;
    private static final Duration CACHE_EXPIRATION = Duration.ofHours(1);

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogExpressionEvaluator.class);

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Cache<String, Expression> expressionCache = AuditLogExpressionEvaluator.newCache();
    private final Cache<String, CompiledTemplate> templateCache = AuditLogExpressionEvaluator.newCache();
    private final Cache<Method, List<String>> parameterNamesCache = AuditLogExpressionEvaluator.newCache();

    /**
     * 创建仅用于当前方法调用的 SpEL 上下文。
     *
     * <p>上下文包含当前调用的参数、返回值和异常，不能跨调用复用。
     *
     * @param method 被拦截的方法
     * @param args 方法参数
     * @param result 方法返回值；方法抛出异常时为 {@code null}
     * @param exception 方法抛出的异常；正常返回时为 {@code null}
     * @return 当前调用的求值上下文
     */
    public EvaluationContext createContext(
            final Method method,
            final Object[] args,
            final @Nullable Object result,
            final @Nullable Throwable exception) {
        final SimpleEvaluationContext context =
                SimpleEvaluationContext.forReadOnlyDataBinding().build();
        final List<String> parameterNames =
                Objects.requireNonNull(this.parameterNamesCache.get(method, this::discoverParameterNames));
        for (int i = 0; i < parameterNames.size() && i < args.length; i++) {
            context.setVariable(parameterNames.get(i), args[i]);
        }
        if (result != null) {
            context.setVariable(AuditLogExpressionEvaluator.RESULT_VARIABLE, result);
        }
        if (exception != null) {
            context.setVariable(AuditLogExpressionEvaluator.EXCEPTION_VARIABLE, exception);
        }
        return context;
    }

    /**
     * 求值模板字符串，将 {@code #{}} 包裹的 SpEL 表达式替换为实际值。
     *
     * @param template 模板字符串
     * @param context 当前方法调用的 SpEL 上下文
     * @return 求值后的字符串
     */
    public String evaluate(final String template, final EvaluationContext context) {
        if (!template.contains(TEMPLATE_PREFIX)) {
            return template;
        }
        final CompiledTemplate compiledTemplate;
        try {
            compiledTemplate = Objects.requireNonNull(this.templateCache.get(template, this::compileTemplate));
        } catch (final RuntimeException ex) {
            AuditLogExpressionEvaluator.LOGGER.warn("Failed to compile audit log SpEL template '{}'", template, ex);
            throw ex;
        }
        return compiledTemplate.evaluate(context);
    }

    private List<String> discoverParameterNames(final Method method) {
        final @Nullable String[] parameterNames = this.parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames == null) {
            return List.of();
        }
        return List.of(parameterNames.clone());
    }

    private CompiledTemplate compileTemplate(final String template) {
        final List<String> literals = new ArrayList<>();
        final List<Expression> expressions = new ArrayList<>();
        int pos = 0;
        while (true) {
            final int start = template.indexOf(TEMPLATE_PREFIX, pos);
            if (start == -1) {
                literals.add(template.substring(pos));
                break;
            }
            final int end = template.indexOf(TEMPLATE_SUFFIX, start + TEMPLATE_PREFIX.length());
            if (end == -1) {
                literals.add(template.substring(pos));
                break;
            }
            literals.add(template.substring(pos, start));
            final String expressionString = template.substring(start + TEMPLATE_PREFIX.length(), end);
            expressions.add(this.getExpression(expressionString));
            pos = end + TEMPLATE_SUFFIX.length();
        }
        return new CompiledTemplate(List.copyOf(literals), List.copyOf(expressions), template, template.length());
    }

    private Expression getExpression(final String expressionString) {
        return Objects.requireNonNull(this.expressionCache.get(expressionString, this.parser::parseExpression));
    }

    private static <K, V> Cache<K, V> newCache() {
        return Caffeine.newBuilder()
                .maximumSize(AuditLogExpressionEvaluator.MAX_CACHE_SIZE)
                .expireAfterAccess(AuditLogExpressionEvaluator.CACHE_EXPIRATION)
                .build();
    }

    /** 缓存模板的字面量片段与已解析表达式，求值时只拼接并执行表达式。 */
    private record CompiledTemplate(
            List<String> literals, List<Expression> expressions, String template, int initialCapacity) {
        private String evaluate(final EvaluationContext context) {
            final StringBuilder result = new StringBuilder(this.initialCapacity);
            for (int i = 0; i < this.expressions.size(); i++) {
                result.append(this.literals.get(i));
                final Expression expression = this.expressions.get(i);
                final Object value;
                try {
                    value = expression.getValue(context);
                } catch (final RuntimeException ex) {
                    AuditLogExpressionEvaluator.LOGGER.warn(
                            "Failed to evaluate audit log SpEL expression '{}' in template '{}'",
                            expression.getExpressionString(),
                            this.template,
                            ex);
                    throw ex;
                }
                if (value != null) {
                    result.append(value);
                } else {
                    AuditLogExpressionEvaluator.LOGGER.warn(
                            "Audit log SpEL expression '{}' returned null for template '{}'",
                            expression.getExpressionString(),
                            this.template);
                    result.append("!fail#").append(expression.getExpressionString());
                }
            }
            return result.append(this.literals.getLast()).toString();
        }
    }
}

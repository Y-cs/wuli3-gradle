package com.kjs.wuli3.mysql.sql;

import com.kjs.wuli3.mysql.autoconfigure.MysqlSqlProperties;
import com.kjs.wuli3.mysql.autoconfigure.MysqlSqlProperties.LoggingLevel;
import java.lang.reflect.Array;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.jspecify.annotations.Nullable;

/**
 * 记录 MyBatis SQL 耗时，并派发执行异常和慢 SQL 事件。
 *
 * <p>普通查询、游标查询和更新直接从 {@link MappedStatement} 读取 SQL；批处理则在
 * {@code flushStatements} 真正提交 JDBC 批次后读取 {@link BatchResult}。所有观测逻辑都是
 * 采用尽力而为策略，日志格式化或告警失败不能改变原始数据库调用结果。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Slf4j
@SuppressWarnings("StringConcatToTextBlock")
@Intercepts({
    @Signature(
            type = Executor.class,
            method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(
            type = Executor.class,
            method = "queryCursor",
            args = {MappedStatement.class, Object.class, RowBounds.class}),
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class}),
    @Signature(
            type = Executor.class,
            method = "flushStatements",
            args = {})
})
public final class SqlObservabilityInterceptor implements Interceptor {

    private static final String TRUNCATED_SUFFIX = "...";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Set<String> SENSITIVE_KEYWORDS =
            Set.of("password", "passwd", "pwd", "token", "secret", "credential", "authorization", "apikey", "api_key");

    private final MysqlSqlProperties properties;
    private final List<SqlAlertNotifier> notifiers;

    public SqlObservabilityInterceptor(final MysqlSqlProperties properties, final List<SqlAlertNotifier> notifiers) {
        this.properties = properties;
        this.notifiers = List.copyOf(notifiers);
    }

    @Override
    public Object intercept(final Invocation invocation) throws Throwable {
        // flushStatements 没有 MappedStatement 入参，必须等执行完成后从 BatchResult 还原批次信息。
        if ("flushStatements".equals(invocation.getMethod().getName())) {
            return this.interceptBatch(invocation);
        }
        final MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        final Object parameter = invocation.getArgs()[1];
        final BoundSql boundSql = statement.getBoundSql(parameter);
        final long started = System.nanoTime();
        Throwable failure = null;
        try {
            return invocation.proceed();
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            final Duration elapsed = Duration.ofNanos(System.nanoTime() - started);
            this.observeSafely(invocation.getMethod().getName(), statement, boundSql, parameter, elapsed, failure);
        }
    }

    private Object interceptBatch(final Invocation invocation) throws Throwable {
        final long started = System.nanoTime();
        try {
            final Object result = invocation.proceed();
            final Duration elapsed = Duration.ofNanos(System.nanoTime() - started);
            // 一个 flush 可能包含多个 Mapper SQL；每个 BatchResult 分别生成观测事件，共享本次 flush 总耗时。
            if (result instanceof List<?> results) {
                for (final Object item : results) {
                    if (item instanceof BatchResult batchResult) {
                        this.observeBatchSafely(batchResult, elapsed);
                    }
                }
            }
            return result;
        } catch (Throwable ex) {
            final Duration elapsed = Duration.ofNanos(System.nanoTime() - started);
            // 批执行失败时 MyBatis 不一定返回 BatchResult，只能保留批次级异常信息。
            this.observeRenderedSafely("batch", "unknown", "", "-", elapsed, ex);
            throw ex;
        }
    }

    /**
     * 隔离观测代码异常，避免参数解析或日志格式化覆盖原始 SQL 返回值及异常。
     */
    private void observeSafely(
            final String operation,
            final MappedStatement statement,
            final BoundSql boundSql,
            @Nullable final Object parameter,
            final Duration elapsed,
            @Nullable final Throwable failure) {
        try {
            this.observe(operation, statement, boundSql, parameter, elapsed, failure);
        } catch (RuntimeException ex) {
            SqlObservabilityInterceptor.log.warn("Failed to observe MyBatis SQL", ex);
        }
    }

    private void observeBatchSafely(final BatchResult batchResult, final Duration elapsed) {
        try {
            this.observeBatch(batchResult, elapsed);
        } catch (RuntimeException ex) {
            SqlObservabilityInterceptor.log.warn("Failed to observe MyBatis batch result", ex);
        }
    }

    private void observeRenderedSafely(
            final String operation,
            final String statementId,
            final String sql,
            final String parameters,
            final Duration elapsed,
            @Nullable final Throwable failure) {
        try {
            this.observeRendered(operation, statementId, sql, parameters, elapsed, failure);
        } catch (RuntimeException ex) {
            SqlObservabilityInterceptor.log.warn("Failed to observe MyBatis SQL", ex);
        }
    }

    private void observeBatch(final BatchResult batchResult, final Duration elapsed) {
        final MappedStatement statement = batchResult.getMappedStatement();
        final String sql = SqlObservabilityInterceptor.compact(batchResult.getSql(), this.properties.getMaxSqlLength());
        final String parameters = this.batchParameterSummary(statement, batchResult.getParameterObjects());
        this.observeRendered("batch", statement.getId(), sql, parameters, elapsed, null);
    }

    private void observe(
            final String operation,
            final MappedStatement statement,
            final BoundSql boundSql,
            @Nullable final Object parameter,
            final Duration elapsed,
            @Nullable final Throwable failure) {
        final String sql = SqlObservabilityInterceptor.compact(boundSql.getSql(), this.properties.getMaxSqlLength());
        final String parameters = this.parameterSummary(statement.getConfiguration(), boundSql, parameter);
        this.observeRendered(operation, statement.getId(), sql, parameters, elapsed, failure);
    }

    private void observeRendered(
            final String operation,
            final String statementId,
            final String sql,
            final String parameters,
            final Duration elapsed,
            @Nullable final Throwable failure) {
        final double elapsedMs = elapsed.toNanos() / 1_000_000.0;
        final SqlAlertContext context = new SqlAlertContext(operation, statementId, sql, elapsed, parameters, failure);
        // 异常 SQL 优先独立告警，不再重复按普通 SQL 或慢 SQL 处理。
        if (failure != null) {
            if (this.properties.isExceptionEnabled()) {
                SqlObservabilityInterceptor.log.error(
                        SqlObservabilityInterceptor.logMessage("Failed MyBatis SQL"),
                        operation,
                        statementId,
                        elapsedMs,
                        sql,
                        parameters,
                        failure);
                this.dispatch(context);
            }
            return;
        }
        if (this.properties.isLoggingEnabled()) {
            this.logNormalSql(operation, statementId, elapsedMs, sql, parameters);
        }
        if (this.isSlow(elapsed)) {
            SqlObservabilityInterceptor.log.warn(
                    SqlObservabilityInterceptor.logMessage("Slow MyBatis SQL"),
                    operation,
                    statementId,
                    elapsedMs,
                    sql,
                    parameters);
            this.dispatch(context);
        }
    }

    /** 根据配置选择普通 SQL 的 INFO 或 DEBUG 日志级别。 */
    private void logNormalSql(
            final String operation,
            final String statementId,
            final double elapsedMs,
            final String sql,
            final String parameters) {
        if (this.properties.getLoggingLevel() == LoggingLevel.INFO) {
            SqlObservabilityInterceptor.log.info(
                    SqlObservabilityInterceptor.logMessage("MyBatis SQL"),
                    operation,
                    statementId,
                    elapsedMs,
                    sql,
                    parameters);
            return;
        }
        SqlObservabilityInterceptor.log.debug(
                SqlObservabilityInterceptor.logMessage("MyBatis SQL"),
                operation,
                statementId,
                elapsedMs,
                sql,
                parameters);
    }

    /** 构建字段顺序固定的多行日志，方便直接阅读和检索。 */
    private static String logMessage(final String title) {
        return title
                + LINE_SEPARATOR
                + "|-- operation : {}"
                + LINE_SEPARATOR
                + "|-- statement : {}"
                + LINE_SEPARATOR
                + "|-- elapsedMs : {}"
                + LINE_SEPARATOR
                + "|-- sql       : {}"
                + LINE_SEPARATOR
                + "`-- params    : {}";
    }

    private boolean isSlow(final Duration elapsed) {
        final Duration threshold = this.properties.getSlowQueryThreshold();
        // 阈值为 0 或负数时视为关闭，防止误配置导致所有 SQL 都触发告警。
        return this.properties.isSlowQueryEnabled()
                && !threshold.isZero()
                && !threshold.isNegative()
                && elapsed.compareTo(threshold) >= 0;
    }

    private void dispatch(final SqlAlertContext context) {
        for (final SqlAlertNotifier notifier : this.notifiers) {
            try {
                notifier.alert(context);
            } catch (RuntimeException ex) {
                // 单个外部告警渠道失败不能阻断其他渠道，更不能影响数据库事务。
                SqlObservabilityInterceptor.log.warn("MyBatis SQL alert notifier failed", ex);
            }
        }
    }

    /**
     * 按批次中的参数对象重新生成 BoundSql，便于正确解析动态 SQL 参数。
     */
    private String batchParameterSummary(final MappedStatement statement, final List<Object> parameterObjects) {
        if (!this.properties.isIncludeParameters()) {
            return "batchSize=" + parameterObjects.size();
        }
        final StringBuilder summary = new StringBuilder();
        for (int index = 0; index < parameterObjects.size(); index++) {
            final Object parameter = parameterObjects.get(index);
            final BoundSql boundSql = statement.getBoundSql(parameter);
            final String item = this.parameterSummary(statement.getConfiguration(), boundSql, parameter);
            final boolean truncated = SqlObservabilityInterceptor.appendBounded(
                    summary, "#" + index + "{" + item + "}", this.properties.getMaxParameterSummaryLength());
            if (truncated) {
                break;
            }
        }
        return summary.toString();
    }

    private String parameterSummary(
            final Configuration configuration, final BoundSql boundSql, @Nullable final Object parameter) {
        if (!this.properties.isIncludeParameters()) {
            return parameter == null ? "-" : parameter.getClass().getSimpleName();
        }
        final List<ParameterMapping> mappings = boundSql.getParameterMappings();
        if (mappings == null || mappings.isEmpty()) {
            return "-";
        }
        final TypeHandlerRegistry typeHandlers = configuration.getTypeHandlerRegistry();
        final MetaObject metaObject = parameter == null ? null : configuration.newMetaObject(parameter);
        final StringBuilder summary = new StringBuilder();
        for (final ParameterMapping mapping : mappings) {
            // OUT 参数由数据库写回，执行前没有可记录的输入值。
            if (mapping.getMode() == ParameterMode.OUT) {
                continue;
            }
            final String name = mapping.getProperty();
            final Object value = SqlObservabilityInterceptor.isSensitive(name)
                    ? "<redacted>"
                    : this.resolveValue(configuration, boundSql, parameter, metaObject, typeHandlers, name);
            final String prefix = summary.isEmpty() ? "" : ", ";
            final boolean truncated = SqlObservabilityInterceptor.appendBounded(
                    summary,
                    prefix + name + '=' + this.formatValue(value),
                    this.properties.getMaxParameterSummaryLength());
            if (truncated) {
                break;
            }
        }
        return summary.toString();
    }

    private @Nullable Object resolveValue(
            final Configuration configuration,
            final BoundSql boundSql,
            @Nullable final Object parameter,
            @Nullable final MetaObject metaObject,
            final TypeHandlerRegistry typeHandlers,
            final String name) {
        // foreach、bind 等动态 SQL 参数由 MyBatis 放入 additionalParameters，优先级最高。
        if (boundSql.hasAdditionalParameter(name)) {
            return boundSql.getAdditionalParameter(name);
        }
        // foreach 对象属性通常形如 __frch_item_0.id，需要先取根对象再解析其属性。
        final int separator = name.indexOf('.');
        if (separator > 0) {
            final String root = name.substring(0, separator);
            if (boundSql.hasAdditionalParameter(root)) {
                final Object additional = boundSql.getAdditionalParameter(root);
                if (additional == null) {
                    return null;
                }
                return configuration.newMetaObject(additional).getValue(name.substring(separator + 1));
            }
        }
        if (parameter == null) {
            return null;
        }
        // String、数字等简单参数由 TypeHandler 直接处理，不应再按 JavaBean 属性读取。
        if (typeHandlers.hasTypeHandler(parameter.getClass())) {
            return parameter;
        }
        if (parameter instanceof Map<?, ?> parameterMap && parameterMap.containsKey(name)) {
            return parameterMap.get(name);
        }
        return metaObject != null && metaObject.hasGetter(name) ? metaObject.getValue(name) : "<unavailable>";
    }

    private String formatValue(@Nullable final Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof byte[] bytes) {
            // 二进制和容器只记录规模，避免日志展开大对象。
            return "<binary:" + bytes.length + ">";
        }
        if (value.getClass().isArray()) {
            return "<array:" + Array.getLength(value) + ">";
        }
        if (value instanceof java.util.Collection<?> collection) {
            return "<collection:" + collection.size() + ">";
        }
        if (value instanceof Map<?, ?> map) {
            return "<map:" + map.size() + ">";
        }
        return SqlObservabilityInterceptor.truncate(
                SqlObservabilityInterceptor.compact(String.valueOf(value), Integer.MAX_VALUE),
                this.properties.getMaxParameterLength());
    }

    private static String compact(final String value, final int maxLength) {
        final String normalized = SqlObservabilityInterceptor.WHITESPACE
                .matcher(value)
                .replaceAll(" ")
                .trim();
        return SqlObservabilityInterceptor.truncate(normalized, maxLength);
    }

    private static String truncate(final String value, final int maxLength) {
        final int effectiveMaxLength = Math.max(1, maxLength);
        if (value.length() <= effectiveMaxLength) {
            return value;
        }
        final int suffixLength = Math.min(TRUNCATED_SUFFIX.length(), effectiveMaxLength);
        final int contentLength = effectiveMaxLength - suffixLength;
        return value.substring(0, contentLength) + TRUNCATED_SUFFIX.substring(0, suffixLength);
    }

    private static boolean appendBounded(final StringBuilder target, final String value, final int maxLength) {
        final int effectiveMaxLength = Math.max(1, maxLength);
        final int remaining = effectiveMaxLength - target.length();
        if (remaining <= 0) {
            return true;
        }
        if (value.length() <= remaining) {
            target.append(value);
            return false;
        }
        // 后缀也计入总长度，保证最终摘要绝不超过配置上限。
        final int suffixLength = Math.min(TRUNCATED_SUFFIX.length(), remaining);
        final int contentLength = remaining - suffixLength;
        target.append(value, 0, contentLength).append(TRUNCATED_SUFFIX, 0, suffixLength);
        return true;
    }

    private static boolean isSensitive(final String name) {
        final String lower = name.toLowerCase(Locale.ROOT);
        return SqlObservabilityInterceptor.SENSITIVE_KEYWORDS.stream().anyMatch(lower::contains);
    }
}

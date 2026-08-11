package com.kjs.wuli3.mysql.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.mysql.autoconfigure.MysqlSqlProperties;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;

class SqlObservabilityInterceptorTest {

    @Test
    void slowUpdateDispatchesStructuredEvent() throws Throwable {
        final MysqlSqlProperties properties = new MysqlSqlProperties();
        properties.setSlowQueryEnabled(true);
        properties.setSlowQueryThreshold(Duration.ofNanos(1));
        final List<SqlAlertContext> events = new ArrayList<>();
        final SqlObservabilityInterceptor interceptor =
                new SqlObservabilityInterceptor(properties, List.of(events::add));
        final MappedStatement statement = SqlObservabilityInterceptorTest.statement();
        final Invocation invocation = mock(Invocation.class);
        final Method method = Executor.class.getMethod("update", MappedStatement.class, Object.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArgs()).thenReturn(new Object[] {statement, null});
        when(invocation.proceed()).thenReturn(1);

        assertThat(interceptor.intercept(invocation)).isEqualTo(1);
        assertThat(events).singleElement().satisfies(event -> {
            assertThat(event.statementId()).isEqualTo("demo.update");
            assertThat(event.operation()).isEqualTo("update");
            assertThat(event.sql()).isEqualTo("update demo set value = ?");
            assertThat(event.error()).isNull();
        });
    }

    @Test
    void parameterSummaryRedactsSensitiveAndBoundsLargeValues() throws Throwable {
        final MysqlSqlProperties properties = new MysqlSqlProperties();
        properties.setSlowQueryEnabled(true);
        properties.setSlowQueryThreshold(Duration.ofNanos(1));
        properties.setIncludeParameters(true);
        final List<SqlAlertContext> events = new ArrayList<>();
        final SqlObservabilityInterceptor interceptor =
                new SqlObservabilityInterceptor(properties, List.of(events::add));
        final MappedStatement statement = SqlObservabilityInterceptorTest.parameterizedStatement();
        final Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod())
                .thenReturn(Executor.class.getMethod("update", MappedStatement.class, Object.class));
        when(invocation.getArgs())
                .thenReturn(new Object[] {statement, Map.of("password", "secret-value", "payload", new byte[4])});
        when(invocation.proceed()).thenReturn(1);

        interceptor.intercept(invocation);

        assertThat(events)
                .singleElement()
                .extracting(SqlAlertContext::parameterSummary)
                .satisfies(summary -> assertThat(summary).contains("password=<redacted>", "payload=<binary:4>"));
    }

    @Test
    void failedSqlDispatchesEventWithoutBeingSlow() throws Throwable {
        final MysqlSqlProperties properties = new MysqlSqlProperties();
        final List<SqlAlertContext> events = new ArrayList<>();
        final SqlObservabilityInterceptor interceptor =
                new SqlObservabilityInterceptor(properties, List.of(events::add));
        final Invocation invocation =
                SqlObservabilityInterceptorTest.updateInvocation(SqlObservabilityInterceptorTest.statement(), Map.of());
        final IllegalStateException failure = new IllegalStateException("database failed");
        when(invocation.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> interceptor.intercept(invocation)).isSameAs(failure);
        assertThat(events).singleElement().satisfies(event -> {
            assertThat(event.error()).isSameAs(failure);
            assertThat(event.operation()).isEqualTo("update");
        });
    }

    @Test
    void queryCursorIsObserved() throws Throwable {
        final MysqlSqlProperties properties = new MysqlSqlProperties();
        properties.setSlowQueryEnabled(true);
        properties.setSlowQueryThreshold(Duration.ofNanos(1));
        final List<SqlAlertContext> events = new ArrayList<>();
        final SqlObservabilityInterceptor interceptor =
                new SqlObservabilityInterceptor(properties, List.of(events::add));
        final Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod())
                .thenReturn(
                        Executor.class.getMethod("queryCursor", MappedStatement.class, Object.class, RowBounds.class));
        when(invocation.getArgs())
                .thenReturn(new Object[] {SqlObservabilityInterceptorTest.statement(), null, RowBounds.DEFAULT});
        when(invocation.proceed()).thenReturn(mock(Cursor.class));

        interceptor.intercept(invocation);

        assertThat(events)
                .singleElement()
                .extracting(SqlAlertContext::operation)
                .isEqualTo("queryCursor");
    }

    @Test
    void batchFlushIsObservedWithResolvedParameters() throws Throwable {
        final MysqlSqlProperties properties = new MysqlSqlProperties();
        properties.setSlowQueryEnabled(true);
        properties.setSlowQueryThreshold(Duration.ofNanos(1));
        properties.setIncludeParameters(true);
        final List<SqlAlertContext> events = new ArrayList<>();
        final SqlObservabilityInterceptor interceptor =
                new SqlObservabilityInterceptor(properties, List.of(events::add));
        final MappedStatement statement = SqlObservabilityInterceptorTest.scalarStatement();
        final BatchResult batchResult = new BatchResult(statement, "insert into demo(value) values (?)", "first");
        batchResult.addParameterObject("second");
        final Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(Executor.class.getMethod("flushStatements"));
        when(invocation.proceed()).thenReturn(List.of(batchResult));

        interceptor.intercept(invocation);

        assertThat(events).singleElement().satisfies(event -> {
            assertThat(event.operation()).isEqualTo("batch");
            assertThat(event.statementId()).isEqualTo("demo.scalar");
            assertThat(event.parameterSummary()).contains("value=first", "value=second");
        });
    }

    @Test
    void additionalParametersTakePrecedenceAndSummaryIsBounded() throws Throwable {
        final MysqlSqlProperties properties = new MysqlSqlProperties();
        properties.setSlowQueryEnabled(true);
        properties.setSlowQueryThreshold(Duration.ofNanos(1));
        properties.setIncludeParameters(true);
        properties.setMaxParameterSummaryLength(20);
        final List<SqlAlertContext> events = new ArrayList<>();
        final SqlObservabilityInterceptor interceptor =
                new SqlObservabilityInterceptor(properties, List.of(events::add));
        final Invocation invocation = SqlObservabilityInterceptorTest.updateInvocation(
                SqlObservabilityInterceptorTest.additionalParameterStatement(), Map.of("id", 999));
        when(invocation.proceed()).thenReturn(1);

        interceptor.intercept(invocation);

        assertThat(events)
                .singleElement()
                .extracting(SqlAlertContext::parameterSummary)
                .satisfies(summary -> {
                    assertThat(summary).startsWith("__frch_item_0.id=7");
                    assertThat(summary).hasSizeLessThanOrEqualTo(20);
                });
    }

    private static MappedStatement statement() {
        final Configuration configuration = new Configuration();
        return new MappedStatement.Builder(
                        configuration,
                        "demo.update",
                        new StaticSqlSource(configuration, " update   demo set value = ? "),
                        SqlCommandType.UPDATE)
                .build();
    }

    private static MappedStatement parameterizedStatement() {
        final Configuration configuration = new Configuration();
        final List<ParameterMapping> mappings = List.of(
                new ParameterMapping.Builder(configuration, "password", String.class).build(),
                new ParameterMapping.Builder(configuration, "payload", byte[].class).build());
        return new MappedStatement.Builder(
                        configuration,
                        "demo.parameterized",
                        new StaticSqlSource(configuration, " update demo set password = ?, payload = ? ", mappings),
                        SqlCommandType.UPDATE)
                .build();
    }

    private static MappedStatement scalarStatement() {
        final Configuration configuration = new Configuration();
        final List<ParameterMapping> mappings =
                List.of(new ParameterMapping.Builder(configuration, "value", String.class).build());
        return new MappedStatement.Builder(
                        configuration,
                        "demo.scalar",
                        new StaticSqlSource(configuration, "insert into demo(value) values (?)", mappings),
                        SqlCommandType.INSERT)
                .build();
    }

    private static MappedStatement additionalParameterStatement() {
        final Configuration configuration = new Configuration();
        final List<ParameterMapping> mappings = List.of(
                new ParameterMapping.Builder(configuration, "__frch_item_0.id", Integer.class).build(),
                new ParameterMapping.Builder(configuration, "description", String.class).build());
        final BoundSql boundSql = new BoundSql(
                configuration,
                "update demo set value = ? where description = ?",
                mappings,
                Map.of("id", 999, "description", "long-description"));
        boundSql.setAdditionalParameter("__frch_item_0", Map.of("id", 7));
        return new MappedStatement.Builder(configuration, "demo.foreach", parameter -> boundSql, SqlCommandType.UPDATE)
                .build();
    }

    private static Invocation updateInvocation(final MappedStatement statement, final Object parameter)
            throws NoSuchMethodException {
        final Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod())
                .thenReturn(Executor.class.getMethod("update", MappedStatement.class, Object.class));
        when(invocation.getArgs()).thenReturn(new Object[] {statement, parameter});
        return invocation;
    }
}

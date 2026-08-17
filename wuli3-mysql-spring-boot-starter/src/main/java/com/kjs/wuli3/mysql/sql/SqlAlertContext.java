package com.kjs.wuli3.mysql.sql;

import java.time.Duration;
import org.jspecify.annotations.Nullable;

/**
 * 慢 SQL 告警的不可变上下文。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record SqlAlertContext(
        String operation,
        String statementId,
        String sql,
        Duration elapsed,
        String parameterSummary,
        @Nullable Throwable error) {}

package com.kjs.wuli3.mysql.autoconfigure;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SQL 观测配置。默认关闭，避免改变应用的日志量和敏感数据边界。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.mysql.sql")
public class MysqlSqlProperties {

    /** 总开关。 */
    private boolean enabled;

    /** 是否记录所有 SQL。 */
    private boolean loggingEnabled;

    /** 普通 SQL 的日志级别。 */
    private LoggingLevel loggingLevel = LoggingLevel.DEBUG;

    /** 是否启用慢 SQL 检测。 */
    private boolean slowQueryEnabled;

    /** 是否记录并告警执行异常的 SQL。 */
    private boolean exceptionEnabled = true;

    /** 慢 SQL 阈值。 */
    private Duration slowQueryThreshold = Duration.ofSeconds(1);

    /** 是否渲染绑定参数；关闭时只记录占位符 SQL。 */
    private boolean includeParameters;

    /** SQL 文本的最大长度。 */
    private int maxSqlLength = 4096;

    /** 单个参数值摘要的最大长度。 */
    private int maxParameterLength = 256;

    /** 一次 SQL 执行的参数摘要总长度。 */
    private int maxParameterSummaryLength = 4096;

    /** 普通 SQL 支持的日志级别。 */
    public enum LoggingLevel {
        /** 以 DEBUG 级别输出，适合开发和问题排查。 */
        DEBUG,
        /** 以 INFO 级别输出，适合需要长期采集普通 SQL 的应用。 */
        INFO
    }
}

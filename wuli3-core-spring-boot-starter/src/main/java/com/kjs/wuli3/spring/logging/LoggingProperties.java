package com.kjs.wuli3.spring.logging;

import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * 定义 Wuli3 日志策略配置。
 *
 * @author GuoYang create on 2026/9/3 10:00
 */
@Getter
@Setter
@ConfigurationProperties(prefix = LoggingProperties.PREFIX)
public class LoggingProperties {

    /** 向后兼容的日志配置前缀。 */
    public static final String PREFIX = "wuli3.logging";

    /** 默认文本日志格式。 */
    public static final String DEFAULT_TEXT_PATTERN =
            "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [%thread] app=${spring.application.name:application} "
                    + "requestId=%X{requestId} traceId=%X{trace_id} spanId=%X{span_id} %logger{36} - %msg%n%wEx";

    /** 默认日志文件名。 */
    public static final String DEFAULT_FILE_NAME = "logs/${spring.application.name:application}.log";

    /** 默认滚动文件名模式。 */
    public static final String DEFAULT_ROLLING_FILE_NAME_PATTERN = "${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz";

    private boolean enabled = true;
    private Format format = Format.TEXT;
    private PatternProperties pattern = new PatternProperties();
    private FileProperties file = new FileProperties();

    /** 支持的日志输出格式。 */
    public enum Format {
        TEXT,
        ECS,
        GELF,
        LOGSTASH;

        String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** 控制台和文件的文本格式配置。 */
    @Getter
    @Setter
    public static class PatternProperties {
        private String console = LoggingProperties.DEFAULT_TEXT_PATTERN;
        private String file = LoggingProperties.DEFAULT_TEXT_PATTERN;
    }

    /** 文件输出和滚动归档配置。 */
    @Getter
    @Setter
    public static class FileProperties {
        private boolean enabled;
        private String name = LoggingProperties.DEFAULT_FILE_NAME;
        private DataSize maxFileSize = DataSize.ofMegabytes(100);
        private int maxHistory = 30;
        private DataSize totalSizeCap = DataSize.ofGigabytes(5);
        private boolean cleanHistoryOnStart;
        private String fileNamePattern = LoggingProperties.DEFAULT_ROLLING_FILE_NAME_PATTERN;
    }
}

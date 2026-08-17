package com.kjs.wuli3.logging.autoconfigure;

import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/** Wuli3 日志策略配置；实际早期日志属性由 {@link LoggingEnvironmentPostProcessor} 应用。 */
@Getter
@Setter
@ConfigurationProperties(prefix = LoggingProperties.PREFIX)
public class LoggingProperties {

    /** 配置前缀。 */
    public static final String PREFIX = "wuli3.logging";

    /** 统一文本日志格式，保留 requestId 以便和 Web 上下文关联。 */
    public static final String DEFAULT_TEXT_PATTERN =
            "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [%thread] app=${spring.application.name:application} "
                    + "requestId=%X{requestId} %logger{36} - %msg%n%wEx";

    /** 默认日志文件名；仅在 file.enabled=true 且应用未配置 logging.file.* 时使用。 */
    public static final String DEFAULT_FILE_NAME = "logs/${spring.application.name:application}.log";

    /** 默认的日期加大小滚动文件名模式。 */
    public static final String DEFAULT_ROLLING_FILE_NAME_PATTERN = "${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz";

    /** 是否启用 Wuli3 默认日志策略。 */
    private boolean enabled = true;

    /** 输出格式；结构化格式由 Spring Boot 3.5 的 Logback 支持。 */
    private Format format = Format.TEXT;

    /** 文本日志格式配置。 */
    private PatternProperties pattern = new PatternProperties();

    /** 文件日志及滚动归档配置。 */
    private FileProperties file = new FileProperties();

    /** 支持的输出格式。 */
    public enum Format {
        /** 可读的固定文本格式。 */
        TEXT,
        /** Elastic Common Schema JSON。 */
        ECS,
        /** Graylog Extended Log Format JSON。 */
        GELF,
        /** Logstash 兼容 JSON。 */
        LOGSTASH;

        String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** 控制台和文件的文本 pattern。 */
    @Getter
    @Setter
    public static class PatternProperties {

        /** 控制台文本 pattern。 */
        private String console = DEFAULT_TEXT_PATTERN;

        /** 文件文本 pattern。 */
        private String file = DEFAULT_TEXT_PATTERN;
    }

    /** 文件输出和 Logback 滚动策略。 */
    @Getter
    @Setter
    public static class FileProperties {

        /** 是否额外写入滚动文件；容器环境默认只写标准输出。 */
        private boolean enabled;

        /** 默认日志文件路径。 */
        private String name = DEFAULT_FILE_NAME;

        /** 单个归档文件最大大小。 */
        private DataSize maxFileSize = DataSize.ofMegabytes(100);

        /** 归档文件保留天数。 */
        private int maxHistory = 30;

        /** 所有归档文件总容量上限。 */
        private DataSize totalSizeCap = DataSize.ofGigabytes(5);

        /** 启动时是否清理超过保留策略的历史文件。 */
        private boolean cleanHistoryOnStart;

        /** 归档文件名模式。 */
        private String fileNamePattern = DEFAULT_ROLLING_FILE_NAME_PATTERN;
    }
}

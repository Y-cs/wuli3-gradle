package com.kjs.wuli3.spring.logging;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * 在 Spring Boot 初始化日志系统前写入可覆盖的默认属性。
 *
 * @author GuoYang create on 2026/9/3 10:00
 */
public final class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String DEFAULTS_PROPERTY_SOURCE = "wuli3LoggingDefaults";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
        if (StringUtils.hasText(environment.getProperty("logging.config"))) {
            return;
        }
        final LoggingProperties properties = Binder.get(environment)
                .bind(LoggingProperties.PREFIX, Bindable.of(LoggingProperties.class))
                .orElseGet(LoggingProperties::new);
        if (!properties.isEnabled()) {
            return;
        }
        this.validate(properties, environment);
        final Map<String, Object> defaults = new LinkedHashMap<>();
        final LoggingProperties.Format format = Objects.requireNonNull(properties.getFormat(), "format");
        if (format == LoggingProperties.Format.TEXT) {
            final LoggingProperties.PatternProperties pattern = Objects.requireNonNull(properties.getPattern());
            this.putDefault(environment, defaults, "logging.pattern.console", pattern.getConsole());
            this.putDefault(environment, defaults, "logging.pattern.file", pattern.getFile());
        } else {
            this.putDefault(environment, defaults, "logging.structured.format.console", format.id());
            this.putDefault(environment, defaults, "logging.structured.format.file", format.id());
        }
        final LoggingProperties.FileProperties file = Objects.requireNonNull(properties.getFile());
        final boolean fileConfigured = file.isEnabled()
                || environment.containsProperty("logging.file.name")
                || environment.containsProperty("logging.file.path");
        if (file.isEnabled()
                && !environment.containsProperty("logging.file.name")
                && !environment.containsProperty("logging.file.path")) {
            defaults.put("logging.file.name", file.getName());
        }
        if (fileConfigured) {
            this.putDefault(
                    environment,
                    defaults,
                    "logging.logback.rollingpolicy.file-name-pattern",
                    file.getFileNamePattern());
            this.putDefault(
                    environment, defaults, "logging.logback.rollingpolicy.max-file-size", file.getMaxFileSize());
            this.putDefault(environment, defaults, "logging.logback.rollingpolicy.max-history", file.getMaxHistory());
            this.putDefault(
                    environment, defaults, "logging.logback.rollingpolicy.total-size-cap", file.getTotalSizeCap());
            this.putDefault(
                    environment,
                    defaults,
                    "logging.logback.rollingpolicy.clean-history-on-start",
                    file.isCleanHistoryOnStart());
        }
        environment.getPropertySources().remove(LoggingEnvironmentPostProcessor.DEFAULTS_PROPERTY_SOURCE);
        if (!defaults.isEmpty()) {
            environment
                    .getPropertySources()
                    .addLast(new MapPropertySource(LoggingEnvironmentPostProcessor.DEFAULTS_PROPERTY_SOURCE, defaults));
        }
    }

    private void validate(final LoggingProperties properties, final ConfigurableEnvironment environment) {
        final LoggingProperties.PatternProperties pattern = Objects.requireNonNull(properties.getPattern());
        if (properties.getFormat() == LoggingProperties.Format.TEXT
                && (!StringUtils.hasText(pattern.getConsole()) || !StringUtils.hasText(pattern.getFile()))) {
            throw new IllegalArgumentException("wuli3.logging.pattern.console and file must not be blank");
        }
        final LoggingProperties.FileProperties file = Objects.requireNonNull(properties.getFile());
        if (file.getMaxFileSize().toBytes() <= 0
                || file.getMaxHistory() < 1
                || file.getTotalSizeCap().toBytes() < 0) {
            throw new IllegalArgumentException("wuli3.logging.file rolling policy is invalid");
        }
        if (file.isEnabled()
                && !environment.containsProperty("logging.file.path")
                && !StringUtils.hasText(environment.getProperty("logging.file.name", file.getName()))) {
            throw new IllegalArgumentException("wuli3.logging.file.name must not be blank");
        }
    }

    private void putDefault(
            final ConfigurableEnvironment environment,
            final Map<String, Object> defaults,
            final String name,
            final Object value) {
        if (!environment.containsProperty(name)) {
            defaults.put(name, value);
        }
    }
}

package com.kjs.wuli3.logging.autoconfigure;

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

/** 在 Spring Boot 初始化 Logback 前写入可覆盖的 Wuli3 默认日志属性。 */
public final class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String DEFAULTS_PROPERTY_SOURCE = "wuli3LoggingDefaults";
    static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 15;

    @Override
    public int getOrder() {
        return LoggingEnvironmentPostProcessor.ORDER;
    }

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
        if (this.hasCustomConfiguration(environment)) {
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
            final LoggingProperties.PatternProperties pattern =
                    Objects.requireNonNull(properties.getPattern(), "pattern");
            this.putDefault(environment, defaults, "logging.pattern.console", pattern.getConsole());
            this.putDefault(environment, defaults, "logging.pattern.file", pattern.getFile());
        } else {
            this.putDefault(environment, defaults, "logging.structured.format.console", format.id());
            this.putDefault(environment, defaults, "logging.structured.format.file", format.id());
        }

        final LoggingProperties.FileProperties file = Objects.requireNonNull(properties.getFile(), "file");
        final boolean fileConfigured = file.isEnabled()
                || environment.containsProperty("logging.file.name")
                || environment.containsProperty("logging.file.path");
        if (file.isEnabled()) {
            this.putDefaultUnlessPresent(
                    environment, defaults, "logging.file.name", "logging.file.path", file.getName());
        }
        if (fileConfigured) {
            this.putDefault(
                    environment,
                    defaults,
                    "logging.logback.rollingpolicy.file-name-pattern",
                    file.getFileNamePattern());
            this.putDefault(
                    environment,
                    defaults,
                    "logging.logback.rollingpolicy.max-file-size",
                    file.getMaxFileSize().toString());
            this.putDefault(environment, defaults, "logging.logback.rollingpolicy.max-history", file.getMaxHistory());
            this.putDefault(
                    environment,
                    defaults,
                    "logging.logback.rollingpolicy.total-size-cap",
                    file.getTotalSizeCap().toString());
            this.putDefault(
                    environment,
                    defaults,
                    "logging.logback.rollingpolicy.clean-history-on-start",
                    file.isCleanHistoryOnStart());
        }
        this.replaceDefaults(environment, defaults);
    }

    private boolean hasCustomConfiguration(final ConfigurableEnvironment environment) {
        final String loggingConfig = environment.getProperty("logging.config");
        return StringUtils.hasText(loggingConfig);
    }

    private void validate(final LoggingProperties properties, final ConfigurableEnvironment environment) {
        final LoggingProperties.PatternProperties pattern = Objects.requireNonNull(properties.getPattern(), "pattern");
        if (properties.getFormat() == LoggingProperties.Format.TEXT
                && (!StringUtils.hasText(pattern.getConsole()) || !StringUtils.hasText(pattern.getFile()))) {
            throw new IllegalArgumentException("wuli3.logging.pattern.console and file must not be blank");
        }
        final LoggingProperties.FileProperties file = Objects.requireNonNull(properties.getFile(), "file");
        if (file.getMaxFileSize().toBytes() <= 0) {
            throw new IllegalArgumentException("wuli3.logging.file.max-file-size must be greater than zero");
        }
        if (file.getMaxHistory() < 1) {
            throw new IllegalArgumentException("wuli3.logging.file.max-history must be at least 1");
        }
        if (file.getTotalSizeCap().toBytes() < 0) {
            throw new IllegalArgumentException("wuli3.logging.file.total-size-cap must not be negative");
        }
        if (file.isEnabled()
                && !environment.containsProperty("logging.file.name")
                && !environment.containsProperty("logging.file.path")
                && !StringUtils.hasText(file.getName())) {
            throw new IllegalArgumentException(
                    "wuli3.logging.file.name must not be blank when file logging is enabled");
        }
        if (fileConfiguredWithoutName(environment, file)) {
            throw new IllegalArgumentException("wuli3.logging.file.name must not be blank");
        }
    }

    private boolean fileConfiguredWithoutName(
            final ConfigurableEnvironment environment, final LoggingProperties.FileProperties file) {
        return file.isEnabled()
                && environment.containsProperty("logging.file.name")
                && !StringUtils.hasText(environment.getProperty("logging.file.name"))
                && !environment.containsProperty("logging.file.path")
                && !StringUtils.hasText(file.getName());
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

    private void putDefaultUnlessPresent(
            final ConfigurableEnvironment environment,
            final Map<String, Object> defaults,
            final String name,
            final String alternativeName,
            final Object value) {
        if (!environment.containsProperty(name) && !environment.containsProperty(alternativeName)) {
            defaults.put(name, value);
        }
    }

    private void replaceDefaults(final ConfigurableEnvironment environment, final Map<String, Object> defaults) {
        environment.getPropertySources().remove(LoggingEnvironmentPostProcessor.DEFAULTS_PROPERTY_SOURCE);
        if (!defaults.isEmpty()) {
            environment
                    .getPropertySources()
                    .addLast(new MapPropertySource(LoggingEnvironmentPostProcessor.DEFAULTS_PROPERTY_SOURCE, defaults));
        }
    }
}

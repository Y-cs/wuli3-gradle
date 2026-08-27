package com.kjs.wuli3.logging.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.unit.DataSize;

class LoggingEnvironmentPostProcessorTest {

    private final LoggingEnvironmentPostProcessor processor = new LoggingEnvironmentPostProcessor();

    @Test
    void appliesTextAndArchiveDefaultsWithLowPrecedence() {
        final MockEnvironment environment = new MockEnvironment().withProperty("spring.application.name", "orders");

        this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(environment.getProperty("logging.pattern.console")).contains("app=orders");
        assertThat(environment.getProperty("logging.pattern.console"))
                .contains("requestId=%X{requestId}", "traceId=%X{trace_id}", "spanId=%X{span_id}");
        assertThat(environment.getProperty("logging.pattern.file")).contains("app=orders");
        assertThat(Objects.requireNonNull(environment
                                .getPropertySources()
                                .get(LoggingEnvironmentPostProcessor.DEFAULTS_PROPERTY_SOURCE))
                        .getProperty("logging.pattern.console"))
                .isEqualTo(LoggingProperties.DEFAULT_TEXT_PATTERN);
        assertThat(environment.getProperty("logging.file.name")).isNull();
        assertThat(environment.getProperty("logging.logback.rollingpolicy.max-file-size"))
                .isNull();
    }

    @Test
    void usesApplicationAsTheTextPatternFallbackName() {
        final MockEnvironment environment = new MockEnvironment();

        this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(environment.getProperty("logging.pattern.console")).contains("app=application");
    }

    @Test
    void enablesFileAndRollingDefaultsWhenRequested() {
        final MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "orders")
                .withProperty("wuli3.logging.file.enabled", "true")
                .withProperty("wuli3.logging.file.max-file-size", "64MB")
                .withProperty("wuli3.logging.file.max-history", "14")
                .withProperty("wuli3.logging.file.total-size-cap", "2GB");

        this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(environment.getProperty("logging.file.name")).isEqualTo("logs/orders.log");
        assertThat(environment.getProperty("logging.logback.rollingpolicy.max-file-size"))
                .isEqualTo(DataSize.ofMegabytes(64).toString());
        assertThat(environment.getProperty("logging.logback.rollingpolicy.max-history"))
                .isEqualTo("14");
        assertThat(environment.getProperty("logging.logback.rollingpolicy.total-size-cap"))
                .isEqualTo(DataSize.ofGigabytes(2).toString());
        assertThat(Objects.requireNonNull(environment
                                .getPropertySources()
                                .get(LoggingEnvironmentPostProcessor.DEFAULTS_PROPERTY_SOURCE))
                        .getProperty("logging.logback.rollingpolicy.file-name-pattern"))
                .isEqualTo(LoggingProperties.DEFAULT_ROLLING_FILE_NAME_PATTERN);
    }

    @Test
    void preservesApplicationLoggingProperties() {
        final MockEnvironment environment = new MockEnvironment()
                .withProperty("logging.pattern.console", "custom-console")
                .withProperty("logging.file.path", "custom-logs")
                .withProperty("logging.logback.rollingpolicy.max-history", "3")
                .withProperty("wuli3.logging.file.enabled", "true");

        this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(environment.getProperty("logging.pattern.console")).isEqualTo("custom-console");
        assertThat(environment.getProperty("logging.file.path")).isEqualTo("custom-logs");
        assertThat(environment.getProperty("logging.file.name")).isNull();
        assertThat(environment.getProperty("logging.logback.rollingpolicy.max-history"))
                .isEqualTo("3");
    }

    @Test
    void appliesSpringBootStructuredFormat() {
        final MockEnvironment environment = new MockEnvironment().withProperty("wuli3.logging.format", "ecs");

        this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(environment.getProperty("logging.structured.format.console")).isEqualTo("ecs");
        assertThat(environment.getProperty("logging.structured.format.file")).isEqualTo("ecs");
        assertThat(environment.getProperty("logging.pattern.console")).isNull();
    }

    @Test
    void doesNothingWhenDisabledOrCustomConfigIsPresent() {
        final MockEnvironment disabled = new MockEnvironment().withProperty("wuli3.logging.enabled", "false");
        final MockEnvironment custom = new MockEnvironment().withProperty("logging.config", "classpath:custom.xml");

        this.processor.postProcessEnvironment(disabled, new SpringApplication(Object.class));
        this.processor.postProcessEnvironment(custom, new SpringApplication(Object.class));

        assertThat(disabled.getProperty("logging.pattern.console")).isNull();
        assertThat(custom.getProperty("logging.pattern.console")).isNull();
    }

    @Test
    void rejectsInvalidArchiveSettings() {
        final MockEnvironment environment = new MockEnvironment().withProperty("wuli3.logging.file.max-history", "0");

        assertThatThrownBy(
                        () -> this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("wuli3.logging.file.max-history must be at least 1");
    }

    @Test
    void registersFromSpringFactoriesDuringApplicationStartup() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.application.name=inventory", "spring.main.banner-mode=off")
                .run()) {
            assertThat(context.getEnvironment().getProperty("logging.pattern.console"))
                    .contains("app=inventory");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestApplication {}
}

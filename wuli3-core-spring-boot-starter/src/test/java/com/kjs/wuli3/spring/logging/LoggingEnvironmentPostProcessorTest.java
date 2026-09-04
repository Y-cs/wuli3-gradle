package com.kjs.wuli3.spring.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class LoggingEnvironmentPostProcessorTest {

    private final LoggingEnvironmentPostProcessor processor = new LoggingEnvironmentPostProcessor();

    @Test
    void appliesDefaultsWithoutOverridingApplicationSettings() {
        final MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "orders")
                .withProperty("logging.pattern.file", "custom-file");

        this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(environment.getProperty("logging.pattern.console")).contains("app=orders");
        assertThat(environment.getProperty("logging.pattern.file")).isEqualTo("custom-file");
    }

    @Test
    void mapsStructuredFormatToSpringBootProperties() {
        final MockEnvironment environment = new MockEnvironment().withProperty("wuli3.logging.format", "ecs");

        this.processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(environment.getProperty("logging.structured.format.console")).isEqualTo("ecs");
        assertThat(environment.getProperty("logging.pattern.console")).isNull();
    }
}

package com.kjs.wuli3.logging.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

    @Test
    void exposesDefaultProperties() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LoggingProperties.class);
            final LoggingProperties properties = context.getBean(LoggingProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getFormat()).isEqualTo(LoggingProperties.Format.TEXT);
            assertThat(properties.getFile().isEnabled()).isFalse();
        });
    }

    @Test
    void bindsStructuredAndArchiveProperties() {
        this.contextRunner
                .withPropertyValues(
                        "wuli3.logging.format=logstash",
                        "wuli3.logging.file.enabled=true",
                        "wuli3.logging.file.name=var/application.log",
                        "wuli3.logging.file.max-history=12")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    final LoggingProperties properties = context.getBean(LoggingProperties.class);
                    assertThat(properties.getFormat()).isEqualTo(LoggingProperties.Format.LOGSTASH);
                    assertThat(properties.getFile().isEnabled()).isTrue();
                    assertThat(properties.getFile().getName()).isEqualTo("var/application.log");
                    assertThat(properties.getFile().getMaxHistory()).isEqualTo(12);
                });
    }

    @Test
    void backsOffWhenDisabled() {
        this.contextRunner
                .withPropertyValues("wuli3.logging.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(LoggingProperties.class));
    }
}

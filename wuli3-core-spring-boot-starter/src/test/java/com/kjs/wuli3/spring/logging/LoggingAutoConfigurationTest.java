package com.kjs.wuli3.spring.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

    @Test
    void exposesBackwardCompatibleLoggingProperties() {
        this.contextRunner
                .withPropertyValues("wuli3.logging.format=logstash", "wuli3.logging.file.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoggingProperties.class);
                    assertThat(context.getBean(LoggingProperties.class).getFormat())
                            .isEqualTo(LoggingProperties.Format.LOGSTASH);
                    assertThat(context.getBean(LoggingProperties.class)
                                    .getFile()
                                    .isEnabled())
                            .isTrue();
                });
    }
}

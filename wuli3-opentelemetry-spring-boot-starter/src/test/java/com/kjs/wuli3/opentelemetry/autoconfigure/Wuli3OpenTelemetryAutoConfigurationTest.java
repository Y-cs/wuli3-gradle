package com.kjs.wuli3.opentelemetry.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kjs.wuli3.opentelemetry.metrics.MetricRecorder;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class Wuli3OpenTelemetryAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Wuli3OpenTelemetryAutoConfiguration.class));

    @Test
    void exposesAgentBackedTelemetryAccess() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TraceContextAccessor.class);
            assertThat(context).hasSingleBean(MetricRecorder.class);
        });
    }

    @Test
    void keepsApplicationProvidedTelemetryBeans() {
        final TraceContextAccessor customAccessor = java.util.Optional::empty;
        final MetricRecorder customRecorder = mock(MetricRecorder.class);
        this.contextRunner
                .withBean(TraceContextAccessor.class, () -> customAccessor)
                .withBean(MetricRecorder.class, () -> customRecorder)
                .run(context -> {
                    assertThat(context.getBean(TraceContextAccessor.class)).isSameAs(customAccessor);
                    assertThat(context.getBean(MetricRecorder.class)).isSameAs(customRecorder);
                });
    }
}

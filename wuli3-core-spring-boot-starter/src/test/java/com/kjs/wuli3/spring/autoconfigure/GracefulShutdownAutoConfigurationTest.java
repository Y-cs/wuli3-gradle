package com.kjs.wuli3.spring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.spring.shutdown.GracefulShutdownCoordinator;
import com.kjs.wuli3.spring.shutdown.ShutdownPhase;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * 验证优雅关闭基础设施的自动配置条件。
 *
 * @author GuoYang create on 2026/9/3 14:55
 */
class GracefulShutdownAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GracefulShutdownAutoConfiguration.class));

    @Test
    void shouldConfigureInfrastructureByDefault() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GracefulShutdownCoordinator.class);
        });
    }

    @Test
    void shouldAllowShutdownInfrastructureToBeDisabled() {
        this.contextRunner
                .withPropertyValues("wuli3.spring.shutdown.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GracefulShutdownCoordinator.class));
    }

    @Test
    void shouldBindPerPhaseTimeoutOverrides() {
        this.contextRunner
                .withPropertyValues(
                        "wuli3.spring.shutdown.phase-timeout=20s",
                        "wuli3.spring.shutdown.phases.drain-async-tasks.timeout=3s")
                .run(context -> {
                    final GracefulShutdownProperties properties = context.getBean(GracefulShutdownProperties.class);
                    assertThat(properties.getTimeout(ShutdownPhase.CLOSE_CLIENTS))
                            .isEqualTo(Duration.ofSeconds(20));
                    assertThat(properties.getTimeout(ShutdownPhase.DRAIN_ASYNC_TASKS))
                            .isEqualTo(Duration.ofSeconds(3));
                });
    }

    @Test
    void shouldRegisterGracefulShutdownForAutoConfigurationImport() {
        assertThat(ImportCandidates.load(
                        AutoConfiguration.class, this.getClass().getClassLoader()))
                .contains(GracefulShutdownAutoConfiguration.class.getName());
    }
}

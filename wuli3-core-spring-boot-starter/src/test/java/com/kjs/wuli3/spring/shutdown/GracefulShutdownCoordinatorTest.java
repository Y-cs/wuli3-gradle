package com.kjs.wuli3.spring.shutdown;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.SmartLifecycle;

/** 验证阶段顺序和 Spring 生命周期集成。 */
class GracefulShutdownCoordinatorTest {
    @Test
    void coordinatorRunsAfterSpringBootWebServerShutdownPhase() {
        assertThat(GracefulShutdownCoordinator.SHUTDOWN_PHASE).isEqualTo(SmartLifecycle.DEFAULT_PHASE - 2048);
    }

    @Test
    void shouldExecuteAllPhasesInOrder() {
        final ShutdownHookRegistry registry = new ShutdownHookRegistry();
        final List<ShutdownPhase> executions = new ArrayList<>();
        for (final ShutdownPhase phase : ShutdownPhase.values()) {
            registry.register(phase, context -> executions.add(phase), 0);
        }
        final GracefulShutdownCoordinator coordinator =
                new GracefulShutdownCoordinator(registry, phase -> Duration.ofSeconds(1));
        coordinator.start();
        coordinator.stop();
        assertThat(executions).containsExactly(ShutdownPhase.values());
    }

    @Test
    void shouldContinueAfterHookFailure() {
        final ShutdownHookRegistry registry = new ShutdownHookRegistry();
        final List<String> executions = new ArrayList<>();
        registry.register(
                ShutdownPhase.DRAIN_ASYNC_TASKS,
                context -> {
                    throw new IllegalStateException("expected");
                },
                0);
        registry.register(ShutdownPhase.DRAIN_ASYNC_TASKS, context -> executions.add("after-failure"), 10);
        registry.register(ShutdownPhase.AWAIT_REMOTE_ACK, context -> executions.add("next-phase"), 0);

        final GracefulShutdownCoordinator coordinator =
                new GracefulShutdownCoordinator(registry, phase -> Duration.ofSeconds(1));
        coordinator.start();
        coordinator.stop();

        assertThat(executions).containsExactly("after-failure", "next-phase");
    }
}

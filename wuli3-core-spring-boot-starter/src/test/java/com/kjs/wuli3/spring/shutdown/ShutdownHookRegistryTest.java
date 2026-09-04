package com.kjs.wuli3.spring.shutdown;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 验证关闭钩子注册表的排序和关闭后保护。 */
class ShutdownHookRegistryTest {
    @Test
    void shouldOrderHooksByPriority() throws InterruptedException {
        final ShutdownHookRegistry registry = new ShutdownHookRegistry();
        final List<String> names = new ArrayList<>();
        registry.register(ShutdownPhase.DRAIN_ASYNC_TASKS, context -> names.add("last"), 20);
        registry.register(ShutdownPhase.DRAIN_ASYNC_TASKS, context -> names.add("first"), 0);
        registry.register(ShutdownPhase.DRAIN_ASYNC_TASKS, context -> names.add("middle"), 10);
        final ShutdownContext context = new ShutdownContext(Instant.now(), Duration.ofSeconds(1));
        for (final ShutdownHook hook : registry.getHooks(ShutdownPhase.DRAIN_ASYNC_TASKS)) {
            hook.shutdown(context);
        }
        assertThat(names).containsExactly("first", "middle", "last");
    }

    @Test
    void shouldRejectRegistrationAfterShutdownStarts() {
        final ShutdownHookRegistry registry = new ShutdownHookRegistry();
        registry.closeRegistration();
        assertThat(registry.isRegistrationAllowed()).isFalse();
        assertThatThrownBy(() -> registry.register(ShutdownPhase.CLOSE_CLIENTS, context -> {}, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}

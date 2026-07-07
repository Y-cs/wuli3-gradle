package com.kjs.wuli3.rocketmq;

import com.kjs.wuli3.rocketmq.autoconfigure.RocketMqAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RocketMqAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(RocketMqAutoConfiguration.class));

    @Test
    void contextLoads() {
        contextRunner.run(context -> {});
    }
}

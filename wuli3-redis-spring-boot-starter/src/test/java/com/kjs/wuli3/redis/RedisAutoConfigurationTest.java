package com.kjs.wuli3.redis;

import com.kjs.wuli3.redis.autoconfigure.RedisAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RedisAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class));

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
        });
    }
}

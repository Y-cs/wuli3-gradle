package com.kjs.wuli3.mongodb;

import com.kjs.wuli3.mongodb.autoconfigure.MongodbAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MongodbAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(MongodbAutoConfiguration.class));

    @Test
    void contextLoads() {
        contextRunner.run(context -> {});
    }
}

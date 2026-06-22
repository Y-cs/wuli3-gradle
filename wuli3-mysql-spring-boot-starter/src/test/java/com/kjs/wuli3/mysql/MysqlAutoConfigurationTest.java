package com.kjs.wuli3.mysql;

import com.kjs.wuli3.mysql.autoconfigure.MysqlAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MysqlAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MysqlAutoConfiguration.class));

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
        });
    }
}

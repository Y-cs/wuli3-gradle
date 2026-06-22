package com.kjs.wuli3.elasticsearch;

import com.kjs.wuli3.elasticsearch.autoconfigure.ElasticsearchAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ElasticsearchAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ElasticsearchAutoConfiguration.class));

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
        });
    }
}

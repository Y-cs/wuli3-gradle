package com.kjs.wuli3.mysql;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.kjs.wuli3.mysql.autoconfigure.MysqlAutoConfiguration;
import com.kjs.wuli3.mysql.autoconfigure.MysqlSqlProperties;
import com.kjs.wuli3.mysql.autoconfigure.MysqlSqlProperties.LoggingLevel;
import com.kjs.wuli3.mysql.sql.SqlObservabilityInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MysqlAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(MysqlAutoConfiguration.class));

    @Test
    void contextLoads() {
        contextRunner.run(context -> {});
    }

    @Test
    void sqlObservabilityIsDisabledByDefault() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(SqlObservabilityInterceptor.class));
    }

    @Test
    void sqlObservabilityCanBeEnabled() {
        contextRunner
                .withPropertyValues("wuli3.mysql.sql.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(SqlObservabilityInterceptor.class));
    }

    @Test
    void normalSqlLogLevelCanBeConfiguredAsInfo() {
        contextRunner
                .withPropertyValues("wuli3.mysql.sql.logging-level=info")
                .run(context -> assertThat(
                                context.getBean(MysqlSqlProperties.class).getLoggingLevel())
                        .isEqualTo(LoggingLevel.INFO));
    }

    @Test
    void blockAttackProtectionIsEnabledByDefault() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(BlockAttackInnerInterceptor.class));
    }

    @Test
    void blockAttackProtectionCanBeDisabled() {
        contextRunner
                .withPropertyValues("wuli3.mysql.block-attack.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(BlockAttackInnerInterceptor.class));
    }
}

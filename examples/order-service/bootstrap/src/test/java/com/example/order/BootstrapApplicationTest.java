package com.example.order;

import com.example.order.infra.persistence.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 验证所有业务模块可以被 Bootstrap 完整组装。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@SpringBootTest
@MockitoBean(types = OrderMapper.class)
@ImportAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            JdbcTemplateAutoConfiguration.class
        })
final class BootstrapApplicationTest {
    /** 验证 Spring 应用上下文可以启动。 */
    @Test
    void contextLoads() {}
}

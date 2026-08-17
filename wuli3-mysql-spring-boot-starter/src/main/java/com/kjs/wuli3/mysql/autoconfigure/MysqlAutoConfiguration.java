package com.kjs.wuli3.mysql.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.kjs.wuli3.mysql.sql.SqlAlertNotifier;
import com.kjs.wuli3.mysql.sql.SqlObservabilityInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MySQL/MyBatis-Plus 公共能力自动配置。
 *
 * <p>该配置必须先于 MyBatis-Plus 的 InnerInterceptor 聚合配置执行，确保默认的 BlockAttack
 * 拦截器能被收集进最终的 {@code MybatisPlusInterceptor}。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration(before = MybatisPlusInnerInterceptorAutoConfiguration.class)
@ConditionalOnClass(Interceptor.class)
@EnableConfigurationProperties({MysqlSqlProperties.class, MysqlBlockAttackProperties.class})
public class MysqlAutoConfiguration {

    /** 创建默认的全表更新、删除防护拦截器。 */
    @Bean
    @ConditionalOnClass(BlockAttackInnerInterceptor.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "wuli3.mysql.block-attack",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    BlockAttackInnerInterceptor mysqlBlockAttackInnerInterceptor() {
        return new BlockAttackInnerInterceptor();
    }

    /** 创建可覆盖的 MyBatis SQL 观测拦截器。 */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wuli3.mysql.sql", name = "enabled", havingValue = "true")
    SqlObservabilityInterceptor mysqlSqlObservabilityInterceptor(
            final MysqlSqlProperties properties, final ObjectProvider<SqlAlertNotifier> notifiers) {
        return new SqlObservabilityInterceptor(
                properties, notifiers.orderedStream().toList());
    }
}

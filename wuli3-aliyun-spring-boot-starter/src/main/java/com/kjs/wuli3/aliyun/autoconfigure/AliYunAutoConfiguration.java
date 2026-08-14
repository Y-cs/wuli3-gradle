package com.kjs.wuli3.aliyun.autoconfigure;

import com.kjs.wuli3.aliyun.oss.OssClientManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** 根据命名配置创建 AliYun OSS SDK V2 客户端。 */
@AutoConfiguration
@EnableConfigurationProperties(AliYunProperties.class)
public class AliYunAutoConfiguration {

    /** 创建由 Spring 容器统一管理生命周期的 OSS 客户端管理器。 */
    @Bean
    @ConditionalOnMissingBean
    OssClientManager ossClientManager(final AliYunProperties properties) {
        return OssClientManager.create(properties);
    }
}

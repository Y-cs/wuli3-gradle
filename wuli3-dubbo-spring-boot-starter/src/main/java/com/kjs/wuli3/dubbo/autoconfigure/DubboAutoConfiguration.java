package com.kjs.wuli3.dubbo.autoconfigure;

import com.kjs.wuli3.propagation.ContextProxy;
import com.kjs.wuli3.propagation.DefaultContextProxy;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.store.ContextStore;
import org.apache.dubbo.rpc.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 为 Wuli3 Dubbo SPI Filter 提供可由应用覆盖的上下文基础组件。
 *
 * 注意：Filter 实例由 Dubbo 扩展机制创建，这些 Spring Bean 会通过 Dubbo 的 SpringExtensionFactory 注入。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
@AutoConfiguration
@ConditionalOnClass(Filter.class)
@EnableConfigurationProperties(DubboProperties.class)
public class DubboAutoConfiguration {
    /** 创建 Dubbo 调用链默认使用的线程上下文存储。 */
    @Bean
    @ConditionalOnMissingBean
    ContextStore dubboContextStore() {
        return new ContextStore();
    }

    /** 创建负责恢复并关闭 Dubbo provider 调用上下文的传播器。 */
    @Bean
    @ConditionalOnMissingBean
    ContextProxy dubboContextPropagator(final ContextStore contextStore) {
        return new DefaultContextProxy(contextStore);
    }

    /** 创建读写 Dubbo attachments 的标准上下文字段编码器。 */
    @Bean
    @ConditionalOnMissingBean
    ContextPropagator dubboContextEncoder() {
        return new ContextPropagator(ContextPropagator.standardContextEncoder());
    }
}

package com.kjs.wuli3.rocket.autoconfigure;

import com.kjs.wuli3.event.autoconfigure.ConditionalOnMissingRemoteEventTransport;
import com.kjs.wuli3.event.autoconfigure.EventAutoConfiguration;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.rocket.internal.RocketContextSupport;
import com.kjs.wuli3.rocket.internal.RocketPublishOptions;
import com.kjs.wuli3.rocket.internal.RocketRemoteEventTransport;
import com.kjs.wuli3.rocket.internal.RocketV5RemoteEventTransport;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import java.time.Clock;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置默认的 RocketMQ 远程事件传输实现。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration
@AutoConfigureAfter(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
@AutoConfigureBefore(EventAutoConfiguration.class)
@EnableConfigurationProperties(RocketProperties.class)
public class RocketAutoConfiguration {

    /**
     * 创建默认传播调用标识和可信认证信息的上下文编码器。
     */
    @Bean
    @ConditionalOnMissingBean
    ContextPropagator rocketMqContextEncoder() {
        return new ContextPropagator(ContextPropagator.standardContextEncoder());
    }

    /**
     * 创建消费端上下文解码支持；消费适配器自行决定何时恢复和关闭上下文作用域。
     *
     * @param contextWriter  上下文写入器
     * @param contextPropagator 上下文字段编码器
     * @return RocketMQ 上下文支持
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ContextWriter.class)
    RocketContextSupport rocketMqContextSupport(
            final ContextWriter contextWriter, final ContextPropagator contextPropagator) {
        return new RocketContextSupport(contextWriter, contextPropagator);
    }

    /**
     * 创建由不同 RocketMQ 客户端实现共享的事件编码器。
     *
     * @param contextReaders 可选的当前上下文读取器
     * @param contextPropagator 上下文字段编码器
     * @return 公共事件编码器
     */
    @Bean
    @ConditionalOnMissingBean
    RocketMessageWrapperEncoder rocketMqEventMessageEncoder(
            final ObjectProvider<ContextReader> contextReaders, final ContextPropagator contextPropagator) {
        return new RocketMessageWrapperEncoder(contextReaders.getIfUnique(), contextPropagator);
    }

    /**
     * 创建当前生效的 RocketMQ Template 传输实现。
     *
     * @param rocketMQTemplate Apache RocketMQ 模板
     * @param encoder          公共事件编码器
     * @return 远程传输实现
     */
    @Bean
    @ConditionalOnMissingRemoteEventTransport(optionsType = RocketPublishOptions.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    @ConditionalOnProperty(
            prefix = "wuli3.rocketmq",
            name = "client-version",
            havingValue = "v4",
            matchIfMissing = true)
    RocketRemoteEventTransport rocketMqRemoteEventMessageTransport(
            final RocketMQTemplate rocketMQTemplate, final RocketMessageWrapperEncoder encoder) {
        return new RocketRemoteEventTransport(rocketMQTemplate, encoder);
    }

    /**
     * 仅在应用明确选择 v5 且运行时提供 Java Client 时创建 v5 传输。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(
            name = {
                "org.apache.rocketmq.client.apis.ClientServiceProvider",
                "org.apache.rocketmq.client.apis.producer.Producer"
            })
    @ConditionalOnProperty(prefix = "wuli3.rocketmq", name = "client-version", havingValue = "v5")
    static class RocketV5TransportConfiguration {

        /**
         * 使用 Java Client 的 SPI 加载默认服务提供者，应用可通过同类型 Bean 覆盖它。
         *
         * @return Java Client 服务提供者
         */
        @Bean
        @ConditionalOnMissingBean(ClientServiceProvider.class)
        ClientServiceProvider rocketV5ClientServiceProvider() {
            return ClientServiceProvider.loadService();
        }

        /**
         * 使用应用提供并负责生命周期管理的 v5 Producer 创建远程事件传输。
         *
         * @param producer              应用配置的 v5 Producer
         * @param clientServiceProvider Java Client 服务提供者
         * @param encoder               公共事件编码器
         * @return v5 远程事件传输
         */
        @Bean
        @ConditionalOnMissingRemoteEventTransport(optionsType = RocketPublishOptions.class)
        RocketV5RemoteEventTransport rocketV5RemoteEventTransport(
                final Producer producer,
                final ClientServiceProvider clientServiceProvider,
                final RocketMessageWrapperEncoder encoder) {
            return new RocketV5RemoteEventTransport(producer, clientServiceProvider, encoder, Clock.systemUTC());
        }
    }
}

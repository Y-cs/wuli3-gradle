package com.kjs.wuli3.rocket.autoconfigure;

import com.kjs.wuli3.event.autoconfigure.EventAutoConfiguration;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.rocket.internal.RocketContextSupport;
import com.kjs.wuli3.rocket.internal.RocketRemoteEventTransport;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 自动配置默认的 RocketMQ 远程事件传输实现。
 */
@AutoConfiguration
@AutoConfigureAfter(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
@AutoConfigureBefore(EventAutoConfiguration.class)
@ConditionalOnBean(RocketMQTemplate.class)
public class RocketAutoConfiguration {

    /**
     * 创建默认只传播调用标识的上下文编码器。
     */
    @Bean
    @ConditionalOnMissingBean
    ContextEncoder rocketMqContextEncoder() {
        return new ContextEncoder(ContextEncoder.standardContextEncoder());
    }

    /**
     * 创建由不同 RocketMQ 客户端实现共享的事件编码器。
     *
     * @param contextReaders 可选的当前上下文读取器
     * @param contextEncoder 上下文字段编码器
     * @return 公共事件编码器
     */
    @Bean
    @ConditionalOnMissingBean
    RocketMessageWrapperEncoder rocketMqEventMessageEncoder(
            final ObjectProvider<ContextReader> contextReaders, final ContextEncoder contextEncoder) {
        return new RocketMessageWrapperEncoder(contextReaders.getIfUnique(), contextEncoder);
    }

    /**
     * 创建消费端上下文支持，从消息 headers 解码并恢复上下文作用域。
     *
     * @param contextWriter  上下文写入器
     * @param contextEncoder 上下文字段编码器
     * @return RocketMQ 上下文支持
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ContextWriter.class)
    RocketContextSupport rocketMqContextSupport(
            final ContextWriter contextWriter, final ContextEncoder contextEncoder) {
        return new RocketContextSupport(contextWriter, contextEncoder);
    }

    /**
     * 创建当前生效的 RocketMQ Template 传输实现。
     *
     * @param rocketMQTemplate Apache RocketMQ 模板
     * @param encoder          公共事件编码器
     * @return 远程传输实现
     */
    @Bean
    @ConditionalOnMissingBean(RemoteEventTransport.class)
    RocketRemoteEventTransport rocketMqRemoteEventMessageTransport(
            final RocketMQTemplate rocketMQTemplate, final RocketMessageWrapperEncoder encoder) {
        return new RocketRemoteEventTransport(rocketMQTemplate, encoder);
    }
}

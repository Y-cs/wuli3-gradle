package com.kjs.wuli3.rabbit.autoconfigure;

import com.kjs.wuli3.event.autoconfigure.ConditionalOnMissingRemoteEventTransport;
import com.kjs.wuli3.event.autoconfigure.EventAutoConfiguration;
import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.rabbit.internal.RabbitContextSupport;
import com.kjs.wuli3.rabbit.internal.RabbitMessageEncoder;
import com.kjs.wuli3.rabbit.internal.RabbitPublishOptions;
import com.kjs.wuli3.rabbit.internal.RabbitRemoteEventTransport;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;

/** 自动配置默认的 RabbitMQ 远程事件传输实现。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration")
@AutoConfigureBefore(EventAutoConfiguration.class)
public class RabbitAutoConfiguration {

    /** 创建默认传播调用标识和可信认证信息的上下文编码器。 */
    @Bean
    @ConditionalOnMissingBean
    ContextEncoder rabbitMqContextEncoder() {
        return new ContextEncoder(ContextEncoder.standardContextEncoder());
    }

    /**
     * 创建消费端上下文解码支持；消费适配器自行决定何时恢复和关闭上下文作用域。
     *
     * @param contextWriter  上下文写入器
     * @param contextEncoder 上下文字段编码器
     * @return RabbitMQ 上下文支持
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ContextWriter.class)
    RabbitContextSupport rabbitMqContextSupport(
            final ContextWriter contextWriter, final ContextEncoder contextEncoder) {
        return new RabbitContextSupport(contextWriter, contextEncoder);
    }

    /**
     * 创建将事件和传播上下文编码为 AMQP 消息的编码器。
     *
     * @param contextReaders 可选的当前上下文读取器
     * @param contextEncoder 上下文字段编码器
     * @return 事件编码器
     */
    @Bean
    @ConditionalOnMissingBean
    RabbitMessageEncoder rabbitMqEventMessageEncoder(
            final ObjectProvider<ContextReader> contextReaders, final ContextEncoder contextEncoder) {
        return new RabbitMessageEncoder(contextReaders.getIfUnique(), contextEncoder);
    }

    /**
     * 当应用提供 RabbitTemplate 时创建默认传输实现。
     *
     * @param rabbitTemplate      Spring AMQP 模板
     * @param encoder             事件编码器
     * @param applicationExecutor 异步发送执行器
     * @return RabbitMQ 远程事件传输
     */
    @Bean
    @ConditionalOnMissingRemoteEventTransport(optionsType = RabbitPublishOptions.class)
    @ConditionalOnBean(RabbitTemplate.class)
    RabbitRemoteEventTransport rabbitMqRemoteEventMessageTransport(
            final RabbitTemplate rabbitTemplate,
            final RabbitMessageEncoder encoder,
            @Qualifier("applicationTaskExecutor") final TaskExecutor applicationExecutor) {
        return new RabbitRemoteEventTransport(rabbitTemplate, encoder, applicationExecutor);
    }
}

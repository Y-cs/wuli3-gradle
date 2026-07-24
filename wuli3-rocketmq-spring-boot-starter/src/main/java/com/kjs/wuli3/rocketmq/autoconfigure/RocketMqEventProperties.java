package com.kjs.wuli3.rocketmq.autoconfigure;

import java.util.Objects;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** RocketMQ 事件消息发布配置。 */
@Getter
@ConfigurationProperties("wuli3.rocketmq.event")
public class RocketMqEventProperties {

    /** 出站上下文传播模式，默认只传播调用标识。 */
    private RocketMqContextMode contextMode = RocketMqContextMode.INVOCATION_ONLY;

    /** 创建默认只传播调用标识的配置对象。 */
    public RocketMqEventProperties() {}

    /**
     * 设置出站上下文传播模式。
     *
     * @param contextMode 选定的上下文模式
     */
    public void setContextMode(final RocketMqContextMode contextMode) {
        this.contextMode = Objects.requireNonNull(contextMode, "contextMode");
    }
}

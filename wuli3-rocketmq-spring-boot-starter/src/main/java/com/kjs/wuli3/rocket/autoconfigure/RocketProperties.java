package com.kjs.wuli3.rocket.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** RocketMQ 事件传输客户端选择配置。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.rocketmq")
public class RocketProperties {

    /**
     * 事件传输使用的 RocketMQ 客户端版本，默认使用 Spring Boot starter 管理的 v4 客户端。
     */
    private ClientVersion clientVersion = ClientVersion.V4;

    /** 可用于远程事件传输的 RocketMQ 客户端版本。 */
    public enum ClientVersion {
        /** 使用 {@code RocketMQTemplate} 的 v4 客户端。 */
        V4,
        /** 使用 {@code rocketmq-client-java} 的 v5 客户端。 */
        V5
    }
}

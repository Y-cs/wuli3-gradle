package com.kjs.wuli3.dubbo.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置 Wuli3 Dubbo 上下文和错误传播 Filter。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
@Getter
@ConfigurationProperties(prefix = "wuli3.dubbo")
public class DubboProperties {
    /** 上下文传播开关；默认启用，同时作用于 consumer 和 provider Filter。 */
    private final Feature context = new Feature();

    /** 错误传播开关；默认启用，同时作用于 provider 编码和 consumer 解码。 */
    private final Feature error = new Feature();

    /** 表示一项 Dubbo 传播能力的开关配置。 */
    @Getter
    @Setter
    public static class Feature {
        /** 是否启用对应传播能力；默认启用。 */
        private boolean enabled = true;
    }
}

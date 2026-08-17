package com.kjs.wuli3.redis.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Wuli3 Redis 基础设施 Bean 的功能开关。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@ConfigurationProperties(prefix = "wuli3.redis")
public class RedisProperties {

    private final Operations operations = new Operations();
    private final Lock lock = new Lock();

    /** Redis 数据结构操作配置。 */
    @Getter
    @Setter
    public static class Operations {
        /** 是否启用 Redis 数据结构操作。 */
        private boolean enabled = true;
    }

    /** 分布式锁配置。 */
    @Getter
    @Setter
    public static class Lock {
        /** 是否启用 Redis 分布式锁。 */
        private boolean enabled = true;
    }
}

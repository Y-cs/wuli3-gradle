package com.kjs.wuli3.mysql.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** MyBatis-Plus 全表更新和删除防护配置。 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.mysql.block-attack")
public class MysqlBlockAttackProperties {

    /** 是否启用无条件全表更新、删除防护。 */
    private boolean enabled = true;
}

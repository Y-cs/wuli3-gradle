package com.kjs.wuli3.event.autoconfigure;

import com.kjs.wuli3.event.PublishOptions;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

/**
 * 仅在已知传输实现均不支持请求的选项类型时启用默认远程事件传输。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Conditional(OnMissingRemoteEventTransportCondition.class)
public @interface ConditionalOnMissingRemoteEventTransport {

    /**
     * 默认传输实现处理的具体发布选项类型。
     *
     * @return 支持的发布选项类型
     */
    Class<? extends PublishOptions> optionsType();
}

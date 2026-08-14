package com.kjs.wuli3.event.autoconfigure;

import com.kjs.wuli3.event.PublishOptions;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

/**
 * Only enables a default remote event transport when no known transport supports the requested options type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Conditional(OnMissingRemoteEventTransportCondition.class)
public @interface ConditionalOnMissingRemoteEventTransport {

    /**
     * The concrete publish options type handled by the default transport.
     *
     * @return supported publish options type
     */
    Class<? extends PublishOptions> optionsType();
}

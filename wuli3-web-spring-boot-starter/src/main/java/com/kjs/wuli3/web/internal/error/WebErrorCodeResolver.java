package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.core.error.model.ErrorCode;
import com.kjs.wuli3.core.error.resolver.DefaultErrorCodeResolver;
import com.kjs.wuli3.core.error.resolver.ErrorCodeResolver;
import com.kjs.wuli3.web.config.ApplicationServiceProperties;

/**
 * 将对外 Web 错误码格式化为 SERVICE.MODULE.ERROR_NAME。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public class WebErrorCodeResolver implements ErrorCodeResolver {
    private final ErrorCodeResolver delegate;

    public WebErrorCodeResolver(final ApplicationServiceProperties applicationServiceProperties) {
        this.delegate = new DefaultErrorCodeResolver(applicationServiceProperties.getServiceCode());
    }

    @Override
    public String resolve(final ErrorCode errorCode) {
        return this.delegate.resolve(errorCode);
    }
}

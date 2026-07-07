package com.kjs.wuli3.propagation.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 业务代码可读取的认证与授权元数据。
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class AuthContext extends AbstractContext implements PropagationContext {

    private final Long userId;
    private final String username;

    @Override
    public Class<? extends Context> type() {
        return AuthContext.class;
    }
}

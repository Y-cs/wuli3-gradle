package com.kjs.wuli3.propagation.context;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 业务代码可读取的认证与授权元数据。
 */
@Getter
@ToString
@EqualsAndHashCode
public class AuthContext extends AbstractContext implements PropagationContext {

    private final Long userId;
    private final String username;

    public AuthContext(final Long userId, final String username) {
        this(Map.of(), userId, username);
    }

    private AuthContext(final Map<ContextKey<?>, Object> extensions, final Long userId, final String username) {
        super(extensions);
        this.userId = userId;
        this.username = username;
    }

    @Override
    public Context snapshotCopy() {
        return new AuthContext(this.extensionSnapshot(), this.userId, this.username);
    }

    @Override
    public Class<? extends Context> type() {
        return AuthContext.class;
    }
}

package com.kjs.wuli3.propagation.accessor;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.store.ContextReader;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Accessor for authentication context values.
 */
@RequiredArgsConstructor
public class AuthContextAccessor {

    private final ContextReader holder;

    public Optional<AuthContext> current() {
        return holder.get(AuthContext.class);
    }

    public Optional<Long> userId() {
        return current().map(AuthContext::getUserId);
    }

    public Optional<String> username() {
        return current().map(AuthContext::getUsername);
    }
}

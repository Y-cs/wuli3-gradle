package com.kjs.wuli3.propagation.accessor;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.holder.ContextHolder;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * AuthContextAccessor
 *
 * @author GuoYang create on 2026/6/25 15:58
 */
@RequiredArgsConstructor
public class AuthContextAccessor {

    private final ContextHolder holder;

    public Optional<AuthContext> current() {
        return holder.get(AuthContext.class);
    }

    public Optional<Long> currentUserId() {
        return current().map(AuthContext::getUserId);
    }

    public Optional<String> currentUser() {
        return current().map(AuthContext::getUsername);
    }


}

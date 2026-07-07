package com.kjs.wuli3.propagation.codec;

import com.kjs.wuli3.propagation.carrier.ContextCarrierReader;
import com.kjs.wuli3.propagation.carrier.ContextCarrierWriter;
import com.kjs.wuli3.propagation.context.AuthContext;

import java.util.Optional;

/**
 * Propagates authentication metadata for trusted internal call paths.
 */
public final class AuthContextCodec implements PropagationContextCodec<AuthContext> {

    public static final String USER_ID = "X-User-Id";
    public static final String USERNAME = "X-Username";

    @Override
    public Class<AuthContext> contextType() {
        return AuthContext.class;
    }

    @Override
    public Optional<AuthContext> read(final ContextCarrierReader reader) {
        return reader.get(USER_ID)
                .flatMap(AuthContextCodec::parseUserId)
                .map(userId -> new AuthContext(userId, reader.get(USERNAME)
                        .orElse("")));
    }

    @Override
    public void write(final AuthContext context, final ContextCarrierWriter writer) {
        writer.set(USER_ID, String.valueOf(context.getUserId()));
        writer.set(USERNAME, context.getUsername());
    }

    private static Optional<Long> parseUserId(final String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

}

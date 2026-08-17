package com.kjs.wuli3.propagation.encoding;

import com.kjs.wuli3.propagation.context.AuthContext;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * 可信认证元数据的协议字段编解码器。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class AuthContextEncoder implements ContextFieldEncoder<AuthContext> {

    public static final String USER_ID = "X-User-Id";
    public static final String USERNAME = "X-Username";

    @Override
    public Class<AuthContext> contextType() {
        return AuthContext.class;
    }

    @Override
    public Set<String> fieldNames() {
        return Set.of(AuthContextEncoder.USER_ID, AuthContextEncoder.USERNAME);
    }

    @Override
    public void encode(final AuthContext context, final BiConsumer<String, String> fieldWriter) {
        final AuthContext actualContext = Objects.requireNonNull(context, "context");
        final BiConsumer<String, String> actualFieldWriter = Objects.requireNonNull(fieldWriter, "fieldWriter");
        actualFieldWriter.accept(AuthContextEncoder.USER_ID, String.valueOf(actualContext.userId()));
        actualFieldWriter.accept(AuthContextEncoder.USERNAME, actualContext.username());
    }

    @Override
    public Optional<AuthContext> decode(final Function<String, @Nullable String> fieldReader) {
        Objects.requireNonNull(fieldReader, "fieldReader");
        final @Nullable String userIdStr = fieldReader.apply(AuthContextEncoder.USER_ID);
        final @Nullable String username = fieldReader.apply(AuthContextEncoder.USERNAME);
        if (userIdStr == null || username == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new AuthContext(Long.parseLong(userIdStr), username));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}

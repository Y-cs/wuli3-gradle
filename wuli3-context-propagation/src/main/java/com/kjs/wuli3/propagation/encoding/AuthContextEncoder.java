package com.kjs.wuli3.propagation.encoding;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
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

    public static final String PRINCIPAL_TYPE = "X-Principal-Type";
    public static final String PRINCIPAL_ID = "X-Principal-Id";
    public static final String PRINCIPAL_NAME = "X-Principal-Name";

    @Override
    public Class<AuthContext> contextType() {
        return AuthContext.class;
    }

    @Override
    public Set<String> fieldNames() {
        return Set.of(
                AuthContextEncoder.PRINCIPAL_TYPE, AuthContextEncoder.PRINCIPAL_ID, AuthContextEncoder.PRINCIPAL_NAME);
    }

    @Override
    public void encode(final AuthContext context, final BiConsumer<String, String> fieldWriter) {
        final AuthContext actualContext = Objects.requireNonNull(context, "context");
        final BiConsumer<String, String> actualFieldWriter = Objects.requireNonNull(fieldWriter, "fieldWriter");
        actualFieldWriter.accept(
                AuthContextEncoder.PRINCIPAL_TYPE, actualContext.principalType().name());
        actualFieldWriter.accept(AuthContextEncoder.PRINCIPAL_ID, actualContext.principalId());
        actualFieldWriter.accept(AuthContextEncoder.PRINCIPAL_NAME, actualContext.principalName());
    }

    @Override
    @SuppressWarnings("NullAway")
    public Optional<AuthContext> decode(final Function<String, @Nullable String> fieldReader) {
        Objects.requireNonNull(fieldReader, "fieldReader");
        final @Nullable String principalType = fieldReader.apply(AuthContextEncoder.PRINCIPAL_TYPE);
        final @Nullable String principalId = fieldReader.apply(AuthContextEncoder.PRINCIPAL_ID);
        final @Nullable String principalName = fieldReader.apply(AuthContextEncoder.PRINCIPAL_NAME);
        if (principalType == null || principalId == null || principalName == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new AuthContext(PrincipalType.valueOf(principalType), principalId, principalName));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}

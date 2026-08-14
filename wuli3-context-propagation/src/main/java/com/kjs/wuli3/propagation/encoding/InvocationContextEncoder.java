package com.kjs.wuli3.propagation.encoding;

import com.kjs.wuli3.propagation.context.InvocationContext;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * 调用元数据的协议字段编解码器。
 */
public final class InvocationContextEncoder implements ContextFieldEncoder<InvocationContext> {

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String ORIGIN_IP = "X-Origin-Ip";

    @Override
    public Class<InvocationContext> contextType() {
        return InvocationContext.class;
    }

    @Override
    public Set<String> fieldNames() {
        return Set.of(InvocationContextEncoder.REQUEST_ID, InvocationContextEncoder.ORIGIN_IP);
    }

    @Override
    public void encode(final InvocationContext context, final BiConsumer<String, String> fieldWriter) {
        final InvocationContext actualContext = Objects.requireNonNull(context, "context");
        final BiConsumer<String, String> actualFieldWriter = Objects.requireNonNull(fieldWriter, "fieldWriter");
        actualFieldWriter.accept(InvocationContextEncoder.REQUEST_ID, actualContext.requestId());
        actualFieldWriter.accept(InvocationContextEncoder.ORIGIN_IP, actualContext.originIp());
    }

    @Override
    public Optional<InvocationContext> decode(final Function<String, @Nullable String> fieldReader) {
        Objects.requireNonNull(fieldReader, "fieldReader");
        final @Nullable String requestId = fieldReader.apply(InvocationContextEncoder.REQUEST_ID);
        final @Nullable String originIp = fieldReader.apply(InvocationContextEncoder.ORIGIN_IP);
        if (requestId == null || originIp == null) {
            return Optional.empty();
        }
        return Optional.of(new InvocationContext(originIp, requestId));
    }
}

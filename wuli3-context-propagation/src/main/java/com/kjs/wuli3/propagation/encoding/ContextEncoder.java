package com.kjs.wuli3.propagation.encoding;

import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.context.PropagationContext;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * 按显式白名单统一编码和解码协议传播上下文。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class ContextEncoder {

    private final List<ContextFieldEncoder<? extends PropagationContext>> encoders;
    private final Set<String> reservedFieldNames;

    /**
     * 创建一个使用指定上下文编码器的聚合编码器。
     *
     * <p>标准传播字段始终被视为保留字段，即使当前实例只发送其中一部分，
     * 以防业务 header 伪造未启用的传播字段。
     *
     * @param encoders 启用的上下文编码器
     * @throws NullPointerException 当编码器集合或任一编码器为 {@code null} 时
     */
    public ContextEncoder(final Collection<? extends ContextFieldEncoder<? extends PropagationContext>> encoders) {
        this.encoders = List.copyOf(Objects.requireNonNull(encoders, "encoders"));
        this.reservedFieldNames = encoders.stream()
                .map(ContextFieldEncoder::fieldNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public static List<ContextFieldEncoder<? extends PropagationContext>> standardContextEncoder() {
        final List<ContextFieldEncoder<? extends PropagationContext>> encoders = new ArrayList<>();
        encoders.add(new InvocationContextEncoder());
        encoders.add(new AuthContextEncoder());
        return encoders;
    }

    /**
     * 返回所有由该编码器管理的保留字段名。
     *
     * <p>始终返回所有标准传播字段，即使当前编码器只启用了其中一部分。
     * 这样可以防止业务 header 伪造未启用的传播字段。
     *
     * @return 保留字段名
     */
    public Set<String> reservedFieldNames() {
        return reservedFieldNames;
    }

    /**
     * 将快照中的已启用上下文写入协议字段。
     *
     * @param snapshot    待编码快照
     * @param fieldWriter 字段写入器
     * @throws NullPointerException 当参数为 {@code null} 时
     */
    public void writeTo(final ContextSnapshot snapshot, final BiConsumer<String, String> fieldWriter) {
        final ContextSnapshot actualSnapshot = Objects.requireNonNull(snapshot, "snapshot");
        final BiConsumer<String, String> actualFieldWriter = Objects.requireNonNull(fieldWriter, "fieldWriter");
        for (final ContextFieldEncoder<? extends PropagationContext> encoder : this.encoders) {
            ContextEncoder.write(encoder, actualSnapshot, actualFieldWriter);
        }
    }

    /**
     * 从协议字段读取所有已启用的上下文。
     *
     * @param fieldReader 字段读取器；字段不存在时返回 {@code null}
     * @return 解码后的传播上下文快照
     * @throws NullPointerException 当 {@code fieldReader} 为 {@code null} 时
     */
    public ContextSnapshot readFrom(final Function<String, @Nullable String> fieldReader) {
        final Function<String, @Nullable String> actualFieldReader = Objects.requireNonNull(fieldReader, "fieldReader");
        final List<Context> contexts = new ArrayList<>(this.encoders.size());
        for (final ContextFieldEncoder<? extends PropagationContext> encoder : this.encoders) {
            ContextEncoder.read(encoder, actualFieldReader, contexts);
        }
        return ContextSnapshot.of(contexts.toArray(Context[]::new));
    }

    private static <C extends PropagationContext> void write(
            final ContextFieldEncoder<C> encoder,
            final ContextSnapshot snapshot,
            final BiConsumer<String, String> fieldWriter) {
        snapshot.get(encoder.contextType()).ifPresent(context -> encoder.encode(context, fieldWriter));
    }

    @SuppressWarnings("NullAway")
    private static <C extends PropagationContext> void read(
            final ContextFieldEncoder<C> encoder,
            final Function<String, @Nullable String> fieldReader,
            final Collection<Context> contexts) {
        encoder.decode(fieldReader).ifPresent(contexts::add);
    }
}

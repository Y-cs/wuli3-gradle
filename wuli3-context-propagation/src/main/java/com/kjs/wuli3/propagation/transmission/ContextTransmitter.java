package com.kjs.wuli3.propagation.transmission;

import com.kjs.wuli3.propagation.carrier.ContextCarrierReader;
import com.kjs.wuli3.propagation.carrier.ContextCarrierWriter;
import com.kjs.wuli3.propagation.codec.PropagationContextCodec;
import com.kjs.wuli3.propagation.context.PropagationContext;
import com.kjs.wuli3.propagation.snapshot.ContextScope;
import com.kjs.wuli3.propagation.store.ContextContainer;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextWriter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 通过协议适配器读取和写入传播上下文，无需依赖任何特定协议。
 */
public final class ContextTransmitter {

    private final ContextReader contextReader;
    private final ContextWriter contextWriter;
    private final List<PropagationContextCodec<? extends PropagationContext>> codecs;

    public ContextTransmitter(
            final ContextReader contextReader,
            final ContextWriter contextWriter,
            final Collection<? extends PropagationContextCodec<? extends PropagationContext>> codecs) {
        this.contextReader = Objects.requireNonNull(contextReader, "contextReader");
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
        this.codecs = List.copyOf(Objects.requireNonNull(codecs, "codecs"));
    }

    public void writeTo(final ContextCarrierWriter writer) {
        Objects.requireNonNull(writer, "writer");
        for (final PropagationContextCodec<? extends PropagationContext> codec : this.codecs) {
            this.write(codec, writer);
        }
    }

    /**
     * 将入站载体读取到隔离作用域中，并在作用域关闭时恢复此前的线程上下文。
     *
     * <p>解码前会移除所有受管理的上下文，避免载体字段缺失或无效时残留上一请求的身份信息。
     *
     * @param reader 入站协议载体
     * @return 关闭时恢复此前上下文的作用域
     */
    public ContextScope readScoped(final ContextCarrierReader reader) {
        Objects.requireNonNull(reader, "reader");
        final ContextContainer previous = this.contextWriter.snapshot();
        for (final PropagationContextCodec<? extends PropagationContext> codec : this.codecs) {
            this.contextWriter.remove(codec.contextType());
        }
        try {
            for (final PropagationContextCodec<? extends PropagationContext> codec : this.codecs) {
                this.read(codec, reader);
            }
        } catch (final RuntimeException | Error error) {
            try {
                this.restore(previous);
            } catch (final RuntimeException | Error restoreError) {
                error.addSuppressed(restoreError);
            }
            throw error;
        }
        return () -> this.restore(previous);
    }

    private <C extends PropagationContext> void write(
            final PropagationContextCodec<C> codec, final ContextCarrierWriter writer) {
        this.contextReader.get(codec.contextType()).ifPresent(context -> codec.write(context, writer));
    }

    private <C extends PropagationContext> void read(
            final PropagationContextCodec<C> codec, final ContextCarrierReader reader) {
        codec.read(reader).ifPresent(this.contextWriter::put);
    }

    private void restore(final @Nullable ContextContainer previous) {
        if (previous == null) {
            this.contextWriter.clear();
        } else {
            this.contextWriter.restore(previous);
        }
    }
}

package com.kjs.wuli3.propagation.transmission;

import com.kjs.wuli3.propagation.carrier.ContextCarrierReader;
import com.kjs.wuli3.propagation.carrier.ContextCarrierWriter;
import com.kjs.wuli3.propagation.codec.PropagationContextCodec;
import com.kjs.wuli3.propagation.context.PropagationContext;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextWriter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Reads and writes propagation contexts through protocol adapters without depending on any specific protocol.
 */
public final class ContextTransmitter {

    private final ContextReader contextReader;
    private final ContextWriter contextWriter;
    private final List<PropagationContextCodec<? extends PropagationContext>> codecs;

    public ContextTransmitter(final ContextReader contextReader, final ContextWriter contextWriter,
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

    public void readFrom(final ContextCarrierReader reader) {
        Objects.requireNonNull(reader, "reader");
        for (final PropagationContextCodec<? extends PropagationContext> codec : this.codecs) {
            this.read(codec, reader);
        }
    }

    private <C extends PropagationContext> void write(final PropagationContextCodec<C> codec,
            final ContextCarrierWriter writer) {
        this.contextReader.get(codec.contextType())
                .ifPresent(context -> codec.write(context, writer));
    }

    private <C extends PropagationContext> void read(final PropagationContextCodec<C> codec,
            final ContextCarrierReader reader) {
        codec.read(reader)
                .ifPresent(this.contextWriter::put);
    }
}

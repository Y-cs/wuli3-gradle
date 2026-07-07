package com.kjs.wuli3.propagation.codec;

import com.kjs.wuli3.propagation.carrier.ContextCarrierReader;
import com.kjs.wuli3.propagation.carrier.ContextCarrierWriter;
import com.kjs.wuli3.propagation.context.PropagationContext;

import java.util.Optional;

/**
 * Converts one propagation context type to and from a protocol-neutral carrier.
 */
public interface PropagationContextCodec<C extends PropagationContext> {

    Class<C> contextType();

    Optional<C> read(ContextCarrierReader reader);

    void write(C context, ContextCarrierWriter writer);
}

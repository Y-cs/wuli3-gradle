package com.kjs.wuli3.propagation.codec;

import com.kjs.wuli3.propagation.carrier.ContextCarrierReader;
import com.kjs.wuli3.propagation.carrier.ContextCarrierWriter;
import com.kjs.wuli3.propagation.context.InvocationContext;

import java.util.Optional;

/**
 * Propagates request identity and original caller address.
 */
public final class InvocationContextCodec implements PropagationContextCodec<InvocationContext> {

    @Override
    public Class<InvocationContext> contextType() {
        return InvocationContext.class;
    }

    @Override
    public Optional<InvocationContext> read(final ContextCarrierReader reader) {
        return reader.get(PropagationFieldNames.REQUEST_ID)
                .filter(requestId -> !requestId.isBlank())
                .map(requestId -> new InvocationContext(reader.get(PropagationFieldNames.ORIGIN_IP)
                        .orElse(""), requestId));
    }

    @Override
    public void write(final InvocationContext context, final ContextCarrierWriter writer) {
        writer.set(PropagationFieldNames.REQUEST_ID, context.getRequestId());
        writer.set(PropagationFieldNames.ORIGIN_IP, context.getOriginIp());
    }
}

package com.kjs.wuli3.propagation.accessor;

import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.holder.ContextReader;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Accessor for request context values.
 */
@RequiredArgsConstructor
public class InvocationContextAccessor {

    private final ContextReader holder;

    public Optional<InvocationContext> current() {
        return holder.get(InvocationContext.class);
    }

    public Optional<String> requestId() {
        return current().map(InvocationContext::getRequestId);
    }

    public Optional<String> originIp() {
        return current().map(InvocationContext::getOriginIp);
    }
}

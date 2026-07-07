package com.kjs.wuli3.propagation.codec;

import com.kjs.wuli3.propagation.context.PropagationContext;

import java.util.List;

/**
 * Factory methods for common codec sets.
 */
public final class DefaultPropagationContextCodecs {

    private DefaultPropagationContextCodecs() {
    }

    public static List<PropagationContextCodec<? extends PropagationContext>> invocationOnly() {
        return List.of(new InvocationContextCodec());
    }

    public static List<PropagationContextCodec<? extends PropagationContext>> trustedInternal() {
        return List.of(new InvocationContextCodec(), new AuthContextCodec());
    }
}

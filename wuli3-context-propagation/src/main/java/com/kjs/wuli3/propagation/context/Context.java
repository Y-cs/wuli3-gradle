package com.kjs.wuli3.propagation.context;

/**
 * Context
 *
 * @author GuoYang create on 2026/6/25 14:45
 */
public sealed interface Context permits ExtendableContext, LocalContext, PropagationContext {

    Class<? extends Context> type();

    /**
     * Returns an isolated value for a captured snapshot. Immutable implementations may return {@code this}; mutable
     * implementations must return an independent copy.
     */
    Context snapshotCopy();
}

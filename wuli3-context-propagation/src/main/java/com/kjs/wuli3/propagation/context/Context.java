package com.kjs.wuli3.propagation.context;

/**
 * Context
 *
 * @author GuoYang create on 2026/6/25 14:45
 */
public sealed interface Context permits ExtendableContext, LocalContext, PropagationContext {

    Class<? extends Context> type();
}

package com.kjs.wuli3.propagation.propagation;

import com.kjs.wuli3.propagation.holder.ContextContainer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 可在后续流程中恢复的调用上下文快照。
 */
@Getter
@RequiredArgsConstructor
public final class ContextSnapshot {
    private final ContextContainer contextContainer;
}

package com.kjs.wuli3.propagation.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * InvocationContext
 *
 * @author GuoYang create on 2026/6/25 15:00
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class InvocationContext extends AbstractContext implements PropagationContext {

    private final String originIp;

    private final String requestId;

    @Override
    public Class<? extends Context> type() {
        return InvocationContext.class;
    }

}

package com.kjs.wuli3.propagation.context;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * InvocationContext
 *
 * @author GuoYang create on 2026/6/25 15:00
 */
@Getter
@ToString
@EqualsAndHashCode
public class InvocationContext extends AbstractContext implements PropagationContext {

    private final String originIp;

    private final String requestId;

    public InvocationContext(final String originIp, final String requestId) {
        this(Map.of(), originIp, requestId);
    }

    private InvocationContext(
            final Map<ContextKey<?>, Object> extensions, final String originIp, final String requestId) {
        super(extensions);
        this.originIp = originIp;
        this.requestId = requestId;
    }

    @Override
    public Context snapshotCopy() {
        return new InvocationContext(this.extensionSnapshot(), this.originIp, this.requestId);
    }

    @Override
    public Class<? extends Context> type() {
        return InvocationContext.class;
    }
}

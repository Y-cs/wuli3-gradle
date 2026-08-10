package com.kjs.wuli3.core.error.policy;

import java.io.Serializable;
import java.util.Objects;

/** {@link ErrorPolicy} 在运行时解析后的不可变策略值。 */
public record ResolvedErrorPolicy(ErrorSeverity severity, ErrorVisibility visibility, ErrorOrigin origin)
        implements Serializable {

    public ResolvedErrorPolicy {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(visibility, "visibility");
        Objects.requireNonNull(origin, "origin");
    }

    public static ResolvedErrorPolicy from(final ErrorPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        return new ResolvedErrorPolicy(policy.severity(), policy.visibility(), policy.origin());
    }

    public ResolvedErrorPolicy withSeverity(final ErrorSeverity newSeverity) {
        return new ResolvedErrorPolicy(newSeverity, this.visibility, this.origin);
    }

    public ResolvedErrorPolicy withVisibility(final ErrorVisibility newVisibility) {
        return new ResolvedErrorPolicy(this.severity, newVisibility, this.origin);
    }

    public ResolvedErrorPolicy withOrigin(final ErrorOrigin newOrigin) {
        return new ResolvedErrorPolicy(this.severity, this.visibility, newOrigin);
    }
}

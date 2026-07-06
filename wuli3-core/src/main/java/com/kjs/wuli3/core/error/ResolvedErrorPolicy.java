package com.kjs.wuli3.core.error;

import java.io.Serializable;

/**
 * Immutable runtime representation of an {@link ErrorPolicy} annotation.
 */
public record ResolvedErrorPolicy(ErrorSeverity severity, ErrorVisibility visibility) implements Serializable {

    public ResolvedErrorPolicy {
        if (severity == null) {
            severity = ErrorSeverity.NORMAL;
        }
        if (visibility == null) {
            visibility = ErrorVisibility.PUBLIC;
        }
    }

    public static ResolvedErrorPolicy from(final ErrorPolicy policy) {
        return new ResolvedErrorPolicy(policy.severity(), policy.visibility());
    }

    public ResolvedErrorPolicy withSeverity(final ErrorSeverity newSeverity) {
        return new ResolvedErrorPolicy(newSeverity, this.visibility);
    }

    public ResolvedErrorPolicy withVisibility(final ErrorVisibility newVisibility) {
        return new ResolvedErrorPolicy(this.severity, newVisibility);
    }
}

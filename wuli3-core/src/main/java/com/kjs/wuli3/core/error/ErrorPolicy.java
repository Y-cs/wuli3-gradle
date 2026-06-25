package com.kjs.wuli3.core.error;

import java.io.Serial;
import java.io.Serializable;

/**
 * ErrorPolicy
 *
 * @author GuoYang create on 2026/6/24 14:15
 */
public record ErrorPolicy(ErrorSeverity severity, ErrorVisibility visibility) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public ErrorPolicy {
        if (severity == null) {
            severity = ErrorSeverity.NORMAL;
        }
        if (visibility == null) {
            visibility = ErrorVisibility.PUBLIC;
        }
    }

    public static ErrorPolicy defaults() {
        return new ErrorPolicy(ErrorSeverity.NORMAL, ErrorVisibility.PUBLIC);
    }

    public ErrorPolicy withSeverity(ErrorSeverity severity) {
        return new ErrorPolicy(severity, visibility);
    }

    public ErrorPolicy withVisibility(ErrorVisibility visibility) {
        return new ErrorPolicy(severity, visibility);
    }

    public ErrorPolicy with(ErrorSeverity severity, ErrorVisibility visibility) {
        return new ErrorPolicy(severity, visibility);
    }

}

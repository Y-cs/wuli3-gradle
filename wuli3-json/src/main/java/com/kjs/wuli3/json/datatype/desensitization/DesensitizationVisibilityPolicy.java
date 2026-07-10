package com.kjs.wuli3.json.datatype.desensitization;

/**
 * Decides whether the current JSON serialization context may expose the original sensitive value.
 */
public interface DesensitizationVisibilityPolicy {

    /**
     * Returns {@code true} when the original value can be written without masking.
     */
    boolean canViewRaw(Desensitized annotation);

    static DesensitizationVisibilityPolicy alwaysMask() {
        return AlwaysMaskDesensitizationVisibilityPolicy.INSTANCE;
    }
}

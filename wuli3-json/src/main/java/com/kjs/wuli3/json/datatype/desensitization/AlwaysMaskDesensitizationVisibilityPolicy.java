package com.kjs.wuli3.json.datatype.desensitization;

/**
 * Default visibility policy that never exposes raw sensitive values.
 */
final class AlwaysMaskDesensitizationVisibilityPolicy implements DesensitizationVisibilityPolicy {
    static final DesensitizationVisibilityPolicy INSTANCE = new AlwaysMaskDesensitizationVisibilityPolicy();

    private AlwaysMaskDesensitizationVisibilityPolicy() {}

    @Override
    public boolean canViewRaw(final Desensitized annotation) {
        return false;
    }
}

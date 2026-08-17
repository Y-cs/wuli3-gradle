package com.kjs.wuli3.json.datatype.desensitization;

/**
 * 始终隐藏敏感原值的默认可见性策略。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
final class AlwaysMaskDesensitizationVisibilityPolicy implements DesensitizationVisibilityPolicy {
    static final DesensitizationVisibilityPolicy INSTANCE = new AlwaysMaskDesensitizationVisibilityPolicy();

    private AlwaysMaskDesensitizationVisibilityPolicy() {}

    @Override
    public boolean canViewRaw(final Desensitized annotation) {
        return false;
    }
}

package com.kjs.wuli3.json.datatype.desensitization;

/**
 * 决定当前 JSON 序列化上下文能否暴露敏感原值。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface DesensitizationVisibilityPolicy {

    /**
     * 原值可不经脱敏直接写出时返回 {@code true}。
     */
    boolean canViewRaw(Desensitized annotation);

    /** 返回始终执行脱敏的默认策略。 */
    static DesensitizationVisibilityPolicy alwaysMask() {
        return AlwaysMaskDesensitizationVisibilityPolicy.INSTANCE;
    }
}

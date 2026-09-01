package com.kjs.wuli3.audit.payload;

import com.kjs.wuli3.propagation.context.PrincipalType;
import java.util.Objects;

/**
 * 审计事件创建时固化的操作主体快照。
 *
 * @param principalType 操作主体类型
 * @param principalId 操作主体的唯一标识
 * @param principalName 操作主体的显示名称
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditPrincipal(PrincipalType principalType, String principalId, String principalName) {

    /** 创建字段完整的操作主体快照。 */
    public AuditPrincipal {
        Objects.requireNonNull(principalType, "principalType");
        principalId = AuditPrincipal.requireNonBlank(principalId, "principalId");
        principalName = AuditPrincipal.requireNonBlank(principalName, "principalName");
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}

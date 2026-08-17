package com.kjs.wuli3.core.error.policy;

import java.io.Serializable;
import java.util.Objects;

/** {@link ErrorPolicy} 在运行时解析后的不可变策略值。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record ResolvedErrorPolicy(ErrorSeverity severity, ErrorVisibility visibility, ErrorOrigin origin)
        implements Serializable {

    public ResolvedErrorPolicy {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(visibility, "visibility");
        Objects.requireNonNull(origin, "origin");
    }

    /** 从声明式错误策略创建不可变运行时策略。 */
    public static ResolvedErrorPolicy from(final ErrorPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        return new ResolvedErrorPolicy(policy.severity(), policy.visibility(), policy.origin());
    }

    /** 返回替换严重程度后的策略副本。 */
    public ResolvedErrorPolicy withSeverity(final ErrorSeverity newSeverity) {
        return new ResolvedErrorPolicy(newSeverity, this.visibility, this.origin);
    }

    /** 返回替换可见性后的策略副本。 */
    public ResolvedErrorPolicy withVisibility(final ErrorVisibility newVisibility) {
        return new ResolvedErrorPolicy(this.severity, newVisibility, this.origin);
    }

    /** 返回替换错误来源后的策略副本。 */
    public ResolvedErrorPolicy withOrigin(final ErrorOrigin newOrigin) {
        return new ResolvedErrorPolicy(this.severity, this.visibility, newOrigin);
    }
}

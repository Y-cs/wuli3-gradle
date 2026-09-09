package {{basePackage}}.domain.{{domainPackage}};

import {{basePackage}}.sharedkernel.EntityId;
import {{basePackage}}.sharedkernel.Status;

/**
 * {{domainType}} 聚合根示例。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public final class {{domainType}} {
    private final EntityId id;
    private Status status;

    /** 创建处于启用状态的聚合。 */
    public {{domainType}}(final EntityId id) {
        this.id = id;
        this.status = Status.ACTIVE;
    }

    /** 返回聚合标识。 */
    public EntityId id() {
        return this.id;
    }

    /** 返回当前生命周期状态。 */
    public Status status() {
        return this.status;
    }

    /** 停用当前聚合。 */
    public void deactivate() {
        this.status = Status.INACTIVE;
    }
}

package com.example.order.domain.order;

import com.example.order.sharedkernel.EntityId;
import com.example.order.sharedkernel.Status;

/**
 * Order 聚合根示例。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public final class Order {
    private final EntityId id;
    private Status status;

    /** 创建处于启用状态的聚合。 */
    public Order(final EntityId id) {
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

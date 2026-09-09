package com.example.order.infra.persistence;

import com.example.order.domain.order.Order;
import com.example.order.sharedkernel.EntityId;

/**
 * Order 的数据库持久化对象示例。
 *
 * @param id 数据库主键
 * @author GuoYang create on 2026/9/7 15:30
 */
public record OrderPo(String id) {
    /** 转换为领域聚合。 */
    public Order toDomain() {
        return new Order(new EntityId(this.id));
    }
}

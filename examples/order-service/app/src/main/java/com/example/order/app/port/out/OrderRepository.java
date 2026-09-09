package com.example.order.app.port.out;

import com.example.order.domain.order.Order;

/**
 * Order 持久化输出端口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public interface OrderRepository {
    /** 加载示例聚合。 */
    Order load();
}

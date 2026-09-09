package com.example.order.app.port.in;

import com.example.order.api.OrderStatusView;

/**
 * Order 状态查询用例端口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public interface OrderUseCase {
    /** 查询示例聚合的状态。 */
    OrderStatusView status();
}

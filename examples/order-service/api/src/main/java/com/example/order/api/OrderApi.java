package com.example.order.api;

/**
 * Order 业务能力的稳定对外契约。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public interface OrderApi {
    /** 查询示例聚合的状态。 */
    OrderStatusView status();
}

package com.example.order.app.internal;

import com.example.order.api.OrderApi;
import com.example.order.api.OrderStatusView;
import com.example.order.app.port.in.OrderUseCase;
import com.example.order.app.port.out.OrderRepository;
import org.springframework.stereotype.Service;

/**
 * 编排 Order 状态查询用例。
 *
 * 注意：实现保持包可见，基础设施模块只能依赖公开的输出端口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@Service
final class OrderApplicationService implements OrderApi, OrderUseCase {
    private final OrderRepository repository;

    OrderApplicationService(final OrderRepository repository) {
        this.repository = repository;
    }

    /** 查询示例聚合并转换为对外视图。 */
    @Override
    public OrderStatusView status() {
        final var aggregate = this.repository.load();
        return new OrderStatusView(aggregate.id(), aggregate.status());
    }
}

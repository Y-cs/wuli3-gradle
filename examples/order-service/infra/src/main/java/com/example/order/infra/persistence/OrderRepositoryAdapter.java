package com.example.order.infra.persistence;

import com.example.order.app.port.out.OrderRepository;
import com.example.order.domain.order.Order;
import com.example.order.sharedkernel.EntityId;
import org.springframework.stereotype.Repository;

/**
 * Order 持久化输出端口的基础设施实现。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@Repository
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderMapper mapper;

    public OrderRepositoryAdapter(final OrderMapper mapper) {
        this.mapper = mapper;
    }

    /** 加载示例聚合。 */
    @Override
    public Order load() {
        final OrderPo stored = this.mapper.selectById("sample-1");
        if (stored == null) {
            return new Order(new EntityId("sample-1"));
        }
        return stored.toDomain();
    }
}

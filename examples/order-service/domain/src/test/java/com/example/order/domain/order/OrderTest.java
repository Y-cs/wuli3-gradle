package com.example.order.domain.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.order.sharedkernel.EntityId;
import com.example.order.sharedkernel.Status;
import org.junit.jupiter.api.Test;

/**
 * 验证 Order 聚合的核心状态规则。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
final class OrderTest {
    /** 验证新聚合默认启用且可以停用。 */
    @Test
    void deactivatesAggregate() {
        final Order aggregate = new Order(new EntityId("sample-1"));

        aggregate.deactivate();

        assertThat(aggregate.status()).isEqualTo(Status.INACTIVE);
    }
}

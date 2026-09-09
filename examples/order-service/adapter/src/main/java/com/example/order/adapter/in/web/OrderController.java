package com.example.order.adapter.in.web;

import com.example.order.api.OrderApi;
import com.example.order.api.OrderStatusView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 将 Order 应用契约适配为 HTTP 接口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@RestController
@RequestMapping("/api/order")
public final class OrderController {
    private final OrderApi api;

    public OrderController(final OrderApi api) {
        this.api = api;
    }

    /** 查询示例聚合状态。 */
    @GetMapping("/status")
    public OrderStatusView status() {
        return this.api.status();
    }
}

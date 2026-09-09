package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * order-service 的 Spring Boot 组装入口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@SpringBootApplication(scanBasePackages = "com.example.order")
public final class BootstrapApplication {
    private BootstrapApplication() {}

    /** 启动完整的单体应用。 */
    public static void main(final String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }
}

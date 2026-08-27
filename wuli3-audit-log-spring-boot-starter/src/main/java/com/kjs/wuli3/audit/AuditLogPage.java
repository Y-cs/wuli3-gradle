package com.kjs.wuli3.audit;

import java.util.List;
import java.util.Objects;

/**
 * 与具体 HTTP、RPC 或持久化框架无关的审计日志分页结果。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogPage(List<AuditLogView> items, long total, int pageNumber, int pageSize) {

    public AuditLogPage {
        items = List.copyOf(Objects.requireNonNull(items, "items"));
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must not be negative");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
    }
}

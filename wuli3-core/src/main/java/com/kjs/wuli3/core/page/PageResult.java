package com.kjs.wuli3.core.page;

import java.util.List;

public record PageResult<T>(List<T> records, long total, int pageNumber, int pageSize) {
    public PageResult {
        records = List.copyOf(records);
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
    }

    public static <T> PageResult<T> empty(PageQuery query) {
        return new PageResult<>(List.of(), 0, query.pageNumber(), query.pageSize());
    }
}

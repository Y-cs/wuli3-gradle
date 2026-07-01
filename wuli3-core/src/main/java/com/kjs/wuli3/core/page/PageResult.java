package com.kjs.wuli3.core.page;

import java.util.List;

public record PageResult<T>(List<T> records, long total, long pageNo, long pageSize) {

    public PageResult {
        records = List.copyOf(records);
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        if (pageNo < 1) {
            throw new IllegalArgumentException("pageNo must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
    }
}

package com.kjs.wuli3.core.page;

public record PageQuery(long pageNo, long pageSize) {

    public PageQuery {
        if (pageNo < 1) {
            throw new IllegalArgumentException("pageNo must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
    }

    public long offset() {
        return (pageNo - 1) * pageSize;
    }
}

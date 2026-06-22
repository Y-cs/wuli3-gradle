package com.kjs.wuli3.core.page;

public record PageQuery(int pageNumber, int pageSize) {
    public PageQuery {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
    }

    public long offset() {
        return (long) (pageNumber - 1) * pageSize;
    }
}

package com.sportedu.backend.common.api;

import java.util.Collections;
import java.util.List;

public class PageResponse<T> {

    private List<T> list;
    private long pageNo;
    private long pageSize;
    private long total;

    public PageResponse() {
    }

    public PageResponse(List<T> list, long pageNo, long pageSize, long total) {
        this.list = list;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
    }

    public static <T> PageResponse<T> empty(long pageNo, long pageSize) {
        return new PageResponse<>(Collections.emptyList(), pageNo, pageSize, 0);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getPageNo() {
        return pageNo;
    }

    public void setPageNo(long pageNo) {
        this.pageNo = pageNo;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}

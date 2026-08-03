package com.kmp.Triply.global.common;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageResponse(List<T> allItems, int page, int size) {
        this.totalElements = allItems.size();
        this.size = size;
        this.page = page;
        this.totalPages = size == 0 ? 1 : (int) Math.ceil((double) allItems.size() / size);

        int from = Math.min(page * size, allItems.size());
        int to = Math.min(from + size, allItems.size());
        this.content = allItems.subList(from, to);
    }

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
    }
}

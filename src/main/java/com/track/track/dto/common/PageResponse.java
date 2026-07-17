package com.track.track.dto.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    private List<T> content;            // 실제 목록
    private int page;                   // 현재 페이지 번호
    private int size;                   // 한 페이지 크기
    private long totalElements;         // 전체 요소 개수
    private int totalPages;             // 전체 페이지 수
    private boolean first;              // 첫 페이지 여부
    private boolean last;               // 마지막 페이지 여부

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}

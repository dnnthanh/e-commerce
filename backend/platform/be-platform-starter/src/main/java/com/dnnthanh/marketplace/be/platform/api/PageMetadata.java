package com.dnnthanh.marketplace.be.platform.api;

import org.springframework.data.domain.Page;

/** Pagination metadata for offset/page based APIs. */
public record PageMetadata(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious)
        implements ApiMetadata {

    public static PageMetadata from(Page<?> page) {
        return new PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());
    }
}

package com.dnnthanh.marketplace.be.platform.api;

/** Pagination metadata for cursor/search-after APIs. */
public record CursorMetadata(long totalElements, int size, String nextCursor, boolean hasNext)
        implements ApiMetadata {}

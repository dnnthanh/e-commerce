package com.dnnthanh.marketplace.be.review.api.api.request.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped public review filters. Pagination is supplied by Spring Pageable. */
@Getter
@Setter
@NoArgsConstructor
public class ReviewSearchRequest {
    private Long productId;
}

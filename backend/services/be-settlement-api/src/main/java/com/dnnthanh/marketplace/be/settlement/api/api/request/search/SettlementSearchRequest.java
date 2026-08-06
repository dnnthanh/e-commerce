package com.dnnthanh.marketplace.be.settlement.api.api.request.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped seller settlement filters. Pagination is supplied by Spring Pageable. */
@Getter
@Setter
@NoArgsConstructor
public class SettlementSearchRequest {
    private Long sellerId;
}

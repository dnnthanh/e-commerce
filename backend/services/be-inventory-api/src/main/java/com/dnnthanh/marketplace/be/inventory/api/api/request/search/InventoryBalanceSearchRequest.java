package com.dnnthanh.marketplace.be.inventory.api.api.request.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped balance-search filters. Pagination is supplied by Spring Pageable. */
@Getter
@Setter
@NoArgsConstructor
public class InventoryBalanceSearchRequest {
    private Long skuId;
    private Long warehouseId;
}

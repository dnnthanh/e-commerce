package com.dnnthanh.marketplace.be.seller.api.api.request.search;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped seller-shop search request. */
@Getter
@Setter
@NoArgsConstructor
public class SellerShopSearchRequest {
    @NotNull private Long sellerId;
}

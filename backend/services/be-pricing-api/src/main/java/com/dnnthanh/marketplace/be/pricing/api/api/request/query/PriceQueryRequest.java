package com.dnnthanh.marketplace.be.pricing.api.api.request.query;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped public effective-price query request. */
@Getter
@Setter
@NoArgsConstructor
public class PriceQueryRequest {
    private Long skuId;
    private Long sellerId;
    private String channel = "WEB";
    private LocalDateTime at;
}

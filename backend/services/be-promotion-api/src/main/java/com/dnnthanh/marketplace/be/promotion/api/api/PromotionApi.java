package com.dnnthanh.marketplace.be.promotion.api.api;

import com.dnnthanh.marketplace.be.promotion.api.api.request.PromotionRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.response.PromotionResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Promotion evaluation contract with line-level targeting context. */
public interface PromotionApi {
    @PostMapping("/promotions/evaluate")
    PromotionResult evaluate(@RequestBody PromotionRequest request);
}

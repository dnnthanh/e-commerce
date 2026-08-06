package com.dnnthanh.marketplace.be.promotion.api.api.request;

import java.math.BigDecimal;
import java.util.List;

public record PromotionRequest(
        BigDecimal subtotal,
        List<String> codes,
        List<PromotionLineRequest> lines,
        String channel,
        String customerSegment) {
    public PromotionRequest {
        codes = codes == null ? List.of() : List.copyOf(codes);
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public PromotionRequest(BigDecimal subtotal, List<String> codes) {
        this(subtotal, codes, List.of(), "WEB", "DEFAULT");
    }
}

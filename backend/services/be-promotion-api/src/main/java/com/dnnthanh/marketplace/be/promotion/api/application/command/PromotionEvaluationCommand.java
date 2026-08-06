package com.dnnthanh.marketplace.be.promotion.api.application.command;

import java.math.BigDecimal;
import java.util.List;

/** Application command for promotion evaluation independent of HTTP transport DTOs. */
public record PromotionEvaluationCommand(
        BigDecimal subtotal,
        List<String> codes,
        List<Line> lines,
        String channel,
        String customerSegment) {

    public PromotionEvaluationCommand {
        codes = codes == null ? List.of() : List.copyOf(codes);
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /** Checkout shortcut when line-level targeting is not supplied by an internal caller. */
    public static PromotionEvaluationCommand checkout(BigDecimal subtotal, List<String> codes) {
        return new PromotionEvaluationCommand(subtotal, codes, List.of(), "WEB", "DEFAULT");
    }

    public record Line(Long sellerId, String skuId, Long categoryId, BigDecimal subtotal) {}
}

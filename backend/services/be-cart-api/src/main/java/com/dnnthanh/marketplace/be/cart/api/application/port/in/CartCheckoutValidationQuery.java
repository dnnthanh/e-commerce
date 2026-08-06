package com.dnnthanh.marketplace.be.cart.api.application.port.in;

import java.math.BigDecimal;
import java.util.List;

/** Inbound query port that revalidates selected cart lines before checkout. */
public interface CartCheckoutValidationQuery {
    ValidationResult validateCurrentUser(String channel);

    ValidationResult validate(String cartId, String channel);

    record ValidationIssue(String sku, String code, BigDecimal expected, BigDecimal actual) {}

    record ValidationResult(
            String cartId, long version, boolean valid, List<ValidationIssue> issues) {}
}

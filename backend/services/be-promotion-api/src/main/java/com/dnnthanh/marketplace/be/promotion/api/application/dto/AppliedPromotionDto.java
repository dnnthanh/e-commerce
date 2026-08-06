package com.dnnthanh.marketplace.be.promotion.api.application.dto;

import java.math.BigDecimal;

/** Applied promotion read DTO exposed by the application boundary. */
public record AppliedPromotionDto(String promotionId, String code, BigDecimal discount) {}

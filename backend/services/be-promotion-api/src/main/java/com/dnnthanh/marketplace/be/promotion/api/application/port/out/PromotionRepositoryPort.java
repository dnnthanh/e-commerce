package com.dnnthanh.marketplace.be.promotion.api.application.port.out;

import com.dnnthanh.marketplace.be.promotion.api.domain.model.Promotion;
import java.time.LocalDateTime;
import java.util.List;

/** Promotion repository boundary. */
public interface PromotionRepositoryPort {

    /** Finds active candidates. @param at instant @return candidates */
    List<Promotion> findActive(LocalDateTime at);
}

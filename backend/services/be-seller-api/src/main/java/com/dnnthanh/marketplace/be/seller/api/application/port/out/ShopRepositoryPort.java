package com.dnnthanh.marketplace.be.seller.api.application.port.out;

import com.dnnthanh.marketplace.be.seller.api.application.query.SellerShopSearchCriteria;
import com.dnnthanh.marketplace.be.seller.api.domain.model.Shop;
import java.util.List;
import java.util.Optional;

/** Seller shop persistence boundary. */
public interface ShopRepositoryPort {

    /** Finds shop. */
    Optional<Shop> find(Long shopId);

    /** Finds seller shops. */
    List<Shop> findBySeller(SellerShopSearchCriteria criteria);

    /** Saves changed shop + history + outbox atomically. */
    Shop saveMaterialChange(Shop before, Shop after, String actor);
}

package com.dnnthanh.marketplace.be.seller.api.application.port.out;

import com.dnnthanh.marketplace.be.seller.api.domain.model.SellerAccount;
import java.util.Optional;

/** Seller aggregate persistence boundary. */
public interface SellerAccountPort {
    Optional<SellerAccount> findBySellerId(Long sellerId);

    SellerAccount save(SellerAccount seller);
}

package com.dnnthanh.marketplace.be.seller.api.application.port.in;

import com.dnnthanh.marketplace.be.seller.api.application.command.UpdateShopCommand;
import com.dnnthanh.marketplace.be.seller.api.application.query.SellerShopSearchCriteria;
import com.dnnthanh.marketplace.be.seller.api.domain.model.Shop;
import java.util.List;

/** Inbound application port for seller shop profile management. */
public interface ShopProfileUseCase {
    Shop get(Long id);

    List<Shop> list(SellerShopSearchCriteria criteria);

    Shop update(Long id, UpdateShopCommand command);
}

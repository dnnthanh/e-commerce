package com.dnnthanh.marketplace.be.seller.api.application.service;

import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.seller.api.application.command.UpdateShopCommand;
import com.dnnthanh.marketplace.be.seller.api.application.port.in.ShopProfileUseCase;
import com.dnnthanh.marketplace.be.seller.api.application.port.out.ShopRepositoryPort;
import com.dnnthanh.marketplace.be.seller.api.application.query.SellerShopSearchCriteria;
import com.dnnthanh.marketplace.be.seller.api.domain.exception.ShopNotFoundException;
import com.dnnthanh.marketplace.be.seller.api.domain.exception.ShopOwnershipException;
import com.dnnthanh.marketplace.be.seller.api.domain.model.Shop;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/** Seller shop-profile use cases with persisted ownership enforcement. */
@UseCase
@RequiredArgsConstructor
public class ShopProfileServiceImplement implements ShopProfileUseCase {
    private final ShopRepositoryPort repository;
    private final UserContext actor;

    @Override
    public Shop get(Long id) {
        return requireShop(id);
    }

    @Override
    public List<Shop> list(SellerShopSearchCriteria criteria) {
        return repository.findBySeller(criteria);
    }

    @Override
    public Shop update(Long id, UpdateShopCommand command) {
        Shop persisted = requireShop(id);
        requireOwnership(persisted, command.sellerId());
        Shop before = snapshot(persisted);
        if (!persisted.updateMaterialInfo(command.name(), command.description())) {
            return persisted;
        }
        return repository.saveMaterialChange(before, persisted, actor.userId());
    }

    private Shop requireShop(Long id) {
        return repository
                .find(id)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found: " + id));
    }

    private static void requireOwnership(Shop persisted, Long sellerId) {
        if (!Objects.equals(persisted.sellerId(), sellerId)) {
            throw new ShopOwnershipException("Shop does not belong to seller");
        }
    }

    private static Shop snapshot(Shop shop) {
        return new Shop(
                shop.id(),
                shop.sellerId(),
                shop.slug(),
                shop.name(),
                shop.description(),
                shop.status(),
                shop.updatedAt());
    }
}

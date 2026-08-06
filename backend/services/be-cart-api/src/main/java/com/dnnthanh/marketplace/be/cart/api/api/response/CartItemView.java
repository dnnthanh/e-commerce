package com.dnnthanh.marketplace.be.cart.api.api.response;

import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.ItemState;
import java.math.BigDecimal;

public record CartItemView(
        Long skuId,
        Long sellerId,
        int quantity,
        BigDecimal priceSnapshot,
        boolean selected,
        ItemState state) {}

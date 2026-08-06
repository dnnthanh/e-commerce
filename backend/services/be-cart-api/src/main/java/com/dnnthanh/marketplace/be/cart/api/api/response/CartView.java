package com.dnnthanh.marketplace.be.cart.api.api.response;

import java.util.List;

public record CartView(String cartKey, long version, List<CartItemView> items) {}

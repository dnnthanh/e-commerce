package com.dnnthanh.marketplace.be.cart.api.adapter.in.web;

import com.dnnthanh.marketplace.be.cart.api.adapter.in.web.mapper.CartApiMapper;
import com.dnnthanh.marketplace.be.cart.api.api.CartApi;
import com.dnnthanh.marketplace.be.cart.api.api.request.MergeCartRequest;
import com.dnnthanh.marketplace.be.cart.api.api.request.PutItemRequest;
import com.dnnthanh.marketplace.be.cart.api.api.response.CartView;
import com.dnnthanh.marketplace.be.cart.api.application.port.in.CartCheckoutValidationQuery;
import com.dnnthanh.marketplace.be.cart.api.application.port.in.CartUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {
    private final CartUseCase carts;
    private final CartCheckoutValidationQuery validation;
    private final CartApiMapper mapper;

    @Override
    public CartView guest(String key) {
        return mapper.toView(carts.guest(key));
    }

    @Override
    public CartView guestPut(String key, PutItemRequest request) {
        return mapper.toView(carts.guestPut(key, mapper.toCommand(request)));
    }

    @Override
    public CartView get() {
        return mapper.toView(carts.get());
    }

    @Override
    public CartView put(PutItemRequest request) {
        return mapper.toView(carts.put(mapper.toCommand(request)));
    }

    @Override
    public CartView remove(Long sellerId, Long skuId, long expectedVersion) {
        return mapper.toView(carts.remove(sellerId, skuId, expectedVersion));
    }

    @Override
    public CartView saveForLater(Long sellerId, Long skuId, long expectedVersion) {
        return mapper.toView(carts.saveForLater(sellerId, skuId, expectedVersion));
    }

    @Override
    public CartView moveToCart(Long sellerId, Long skuId, long expectedVersion) {
        return mapper.toView(carts.moveToCart(sellerId, skuId, expectedVersion));
    }

    @Override
    public CartView merge(MergeCartRequest request) {
        return mapper.toView(carts.merge(mapper.toCommand(request)));
    }

    @Override
    public CartCheckoutValidationQuery.ValidationResult validate(String channel) {
        return validation.validateCurrentUser(channel);
    }
}

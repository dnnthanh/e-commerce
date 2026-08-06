package com.dnnthanh.marketplace.be.cart.api.api;

import com.dnnthanh.marketplace.be.cart.api.api.request.MergeCartRequest;
import com.dnnthanh.marketplace.be.cart.api.api.request.PutItemRequest;
import com.dnnthanh.marketplace.be.cart.api.api.response.CartView;
import com.dnnthanh.marketplace.be.cart.api.application.port.in.CartCheckoutValidationQuery.ValidationResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface CartApi {

    @GetMapping("/cart")
    CartView guest(@RequestParam String cartKey);

    @PutMapping("/cart/items")
    CartView guestPut(@RequestParam String cartKey, @RequestBody PutItemRequest request);

    @GetMapping("/private/cart")
    @PreAuthorize("@authorizationService.hasPermission('CART_VIEW')")
    CartView get();

    @PutMapping("/private/cart/items")
    @PreAuthorize("@authorizationService.hasPermission('CART_VIEW')")
    CartView put(@RequestBody PutItemRequest request);

    @DeleteMapping("/private/cart/items/{sellerId}/{skuId}")
    @PreAuthorize("@authorizationService.hasPermission('CART_VIEW')")
    CartView remove(
            @PathVariable Long sellerId,
            @PathVariable Long skuId,
            @RequestParam long expectedVersion);

    @PostMapping("/private/cart/items/{sellerId}/{skuId}/save-for-later")
    @PreAuthorize("@authorizationService.hasPermission('CART_VIEW')")
    CartView saveForLater(
            @PathVariable Long sellerId,
            @PathVariable Long skuId,
            @RequestParam long expectedVersion);

    @PostMapping("/private/cart/items/{sellerId}/{skuId}/move-to-cart")
    @PreAuthorize("@authorizationService.hasPermission('CART_VIEW')")
    CartView moveToCart(
            @PathVariable Long sellerId,
            @PathVariable Long skuId,
            @RequestParam long expectedVersion);

    @PostMapping("/private/cart/merge")
    @PreAuthorize("@authorizationService.hasPermission('CART_VIEW')")
    CartView merge(@RequestBody MergeCartRequest request);

    @PostMapping("/private/cart/validate")
    @PreAuthorize("@authorizationService.hasPermission('CART_VIEW')")
    ValidationResult validate(@RequestParam(defaultValue = "WEB") String channel);
}

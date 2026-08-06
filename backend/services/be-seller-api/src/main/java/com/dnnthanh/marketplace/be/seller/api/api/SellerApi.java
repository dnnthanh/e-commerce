package com.dnnthanh.marketplace.be.seller.api.api;

import com.dnnthanh.marketplace.be.seller.api.api.request.UpdateShopRequest;
import com.dnnthanh.marketplace.be.seller.api.api.request.search.SellerShopSearchRequest;
import com.dnnthanh.marketplace.be.seller.api.api.response.ShopView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface SellerApi {

    @GetMapping("/shops/{shopId}")
    ShopView get(@PathVariable Long shopId);

    @GetMapping("/private/seller/shops")
    @PreAuthorize("@authorizationService.hasSellerPermission('SELLER_VIEW', #request.sellerId)")
    List<ShopView> list(@Valid @ModelAttribute SellerShopSearchRequest request);

    @PutMapping("/private/seller/shops/{shopId}")
    @PreAuthorize("@authorizationService.hasSellerPermission('SELLER_UPDATE', #request.sellerId())")
    ShopView update(@PathVariable Long shopId, @RequestBody UpdateShopRequest request);
}

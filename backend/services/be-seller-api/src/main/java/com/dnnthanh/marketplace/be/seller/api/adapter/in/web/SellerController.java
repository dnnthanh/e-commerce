package com.dnnthanh.marketplace.be.seller.api.adapter.in.web;

import com.dnnthanh.marketplace.be.seller.api.adapter.in.web.mapper.SellerApiMapper;
import com.dnnthanh.marketplace.be.seller.api.api.SellerApi;
import com.dnnthanh.marketplace.be.seller.api.api.request.UpdateShopRequest;
import com.dnnthanh.marketplace.be.seller.api.api.request.search.SellerShopSearchRequest;
import com.dnnthanh.marketplace.be.seller.api.api.response.ShopView;
import com.dnnthanh.marketplace.be.seller.api.application.port.in.ShopProfileUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SellerController implements SellerApi {
    private final ShopProfileUseCase useCase;
    private final SellerApiMapper mapper;

    @Override
    public ShopView get(Long shopId) {
        return mapper.modelToResponse(useCase.get(shopId));
    }

    @Override
    public List<ShopView> list(SellerShopSearchRequest request) {
        return useCase.list(mapper.toCriteria(request)).stream()
                .map(mapper::modelToResponse)
                .toList();
    }

    @Override
    public ShopView update(Long shopId, UpdateShopRequest request) {
        return mapper.modelToResponse(useCase.update(shopId, mapper.requestToModel(request)));
    }
}

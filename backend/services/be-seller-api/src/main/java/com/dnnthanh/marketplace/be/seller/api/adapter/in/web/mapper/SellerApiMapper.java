package com.dnnthanh.marketplace.be.seller.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.platform.mapping.RequestModelMapper;
import com.dnnthanh.marketplace.be.seller.api.api.request.UpdateShopRequest;
import com.dnnthanh.marketplace.be.seller.api.api.request.search.SellerShopSearchRequest;
import com.dnnthanh.marketplace.be.seller.api.api.response.ShopView;
import com.dnnthanh.marketplace.be.seller.api.application.command.UpdateShopCommand;
import com.dnnthanh.marketplace.be.seller.api.application.query.SellerShopSearchCriteria;
import com.dnnthanh.marketplace.be.seller.api.domain.model.Shop;
import org.mapstruct.Mapper;

/** Maps Seller HTTP contracts to/from application/domain objects. */
@Mapper(config = PlatformMapperConfig.class)
public interface SellerApiMapper
        extends RequestModelMapper<UpdateShopRequest, UpdateShopCommand>,
                ModelResponseMapper<Shop, ShopView> {

    SellerShopSearchCriteria toCriteria(SellerShopSearchRequest request);

    @Override
    UpdateShopCommand requestToModel(UpdateShopRequest request);

    @Override
    ShopView modelToResponse(Shop shop);
}

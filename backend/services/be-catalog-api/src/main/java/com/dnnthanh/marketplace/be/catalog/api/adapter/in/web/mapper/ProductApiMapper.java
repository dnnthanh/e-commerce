package com.dnnthanh.marketplace.be.catalog.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.catalog.api.api.request.search.ProductSearchRequest;
import com.dnnthanh.marketplace.be.catalog.api.api.response.ProductOfferResponse;
import com.dnnthanh.marketplace.be.catalog.api.api.response.ProductResponse;
import com.dnnthanh.marketplace.be.catalog.api.api.response.SkuOwnerView;
import com.dnnthanh.marketplace.be.catalog.api.application.dto.ProductOffer;
import com.dnnthanh.marketplace.be.catalog.api.application.dto.SkuCheckoutSnapshot;
import com.dnnthanh.marketplace.be.catalog.api.application.query.ProductSearchCriteria;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.Product;
import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.platform.mapping.RequestModelMapper;
import org.mapstruct.Mapper;

/** Maps Catalog domain/application objects to HTTP contracts. */
@Mapper(config = PlatformMapperConfig.class)
public interface ProductApiMapper
        extends RequestModelMapper<ProductSearchRequest, ProductSearchCriteria>,
                ModelResponseMapper<Product, ProductResponse> {

    @Override
    ProductSearchCriteria requestToModel(ProductSearchRequest request);

    @Override
    ProductResponse modelToResponse(Product product);

    SkuOwnerView toView(SkuCheckoutSnapshot snapshot);

    ProductOfferResponse toResponse(ProductOffer offer);
}

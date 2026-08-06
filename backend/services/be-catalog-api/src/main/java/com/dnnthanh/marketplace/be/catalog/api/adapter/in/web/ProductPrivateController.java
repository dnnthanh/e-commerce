package com.dnnthanh.marketplace.be.catalog.api.adapter.in.web;

import com.dnnthanh.marketplace.be.catalog.api.adapter.in.web.mapper.ProductApiMapper;
import com.dnnthanh.marketplace.be.catalog.api.api.ProductPrivateApi;
import com.dnnthanh.marketplace.be.catalog.api.api.request.CreateProductRequest;
import com.dnnthanh.marketplace.be.catalog.api.api.response.ProductResponse;
import com.dnnthanh.marketplace.be.catalog.api.application.port.in.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductPrivateController implements ProductPrivateApi {
    private final ProductUseCase useCase;
    private final ProductApiMapper mapper;

    @Override
    public ProductResponse create(CreateProductRequest request) {
        return mapper.modelToResponse(
                useCase.create(
                        request.sellerId(),
                        request.categoryId(),
                        request.name(),
                        request.description()));
    }

    @Override
    public ProductResponse publish(Long productId) {
        return mapper.modelToResponse(useCase.publish(productId));
    }
}

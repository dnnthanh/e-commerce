package com.dnnthanh.marketplace.be.catalog.api.adapter.in.web;

import com.dnnthanh.marketplace.be.catalog.api.adapter.in.web.mapper.ProductApiMapper;
import com.dnnthanh.marketplace.be.catalog.api.api.ProductPublicApi;
import com.dnnthanh.marketplace.be.catalog.api.api.request.search.ProductSearchRequest;
import com.dnnthanh.marketplace.be.catalog.api.api.response.ProductResponse;
import com.dnnthanh.marketplace.be.catalog.api.application.port.in.ProductUseCase;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.api.PageMetadata;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductPublicController implements ProductPublicApi {
    private final ProductUseCase useCase;
    private final ProductApiMapper mapper;

    @Override
    public ApiResponse<List<ProductResponse>> search(
            ProductSearchRequest request, Pageable pageable) {
        Page<ProductResponse> page =
                useCase.search(mapper.requestToModel(request), pageable)
                        .map(mapper::modelToResponse);
        return ApiResponse.success(page.getContent(), PageMetadata.from(page));
    }

    @Override
    public ProductResponse get(Long productId) {
        return mapper.modelToResponse(useCase.getPublished(productId));
    }
}

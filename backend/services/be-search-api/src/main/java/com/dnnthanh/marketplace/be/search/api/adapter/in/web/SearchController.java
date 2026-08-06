package com.dnnthanh.marketplace.be.search.api.adapter.in.web;

import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.api.CursorMetadata;
import com.dnnthanh.marketplace.be.search.api.adapter.in.web.mapper.SearchApiMapper;
import com.dnnthanh.marketplace.be.search.api.api.SearchApi;
import com.dnnthanh.marketplace.be.search.api.api.request.search.ProductSearchRequest;
import com.dnnthanh.marketplace.be.search.api.api.response.SearchResponse;
import com.dnnthanh.marketplace.be.search.api.application.port.in.ProductSearchUseCase;
import com.dnnthanh.marketplace.be.search.api.application.port.out.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController implements SearchApi {
    private final ProductSearchUseCase searchUseCase;
    private final SearchApiMapper mapper;

    @Override
    public ApiResponse<SearchResponse> search(ProductSearchRequest request) {
        ProductSearchPort.SearchPage page = searchUseCase.search(mapper.toCriteria(request));
        CursorMetadata metadata =
                new CursorMetadata(
                        page.total(),
                        request.getSize(),
                        page.nextCursor(),
                        page.nextCursor() != null);
        return ApiResponse.success(mapper.toResponse(page), metadata);
    }
}

package com.dnnthanh.marketplace.be.search.api.api;

import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.search.api.api.request.search.ProductSearchRequest;
import com.dnnthanh.marketplace.be.search.api.api.response.SearchResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/** Public product discovery API using cursor/search-after pagination for deep result sets. */
@RequestMapping("/search")
public interface SearchApi {
    @GetMapping
    ApiResponse<SearchResponse> search(@Valid @ModelAttribute ProductSearchRequest request);
}

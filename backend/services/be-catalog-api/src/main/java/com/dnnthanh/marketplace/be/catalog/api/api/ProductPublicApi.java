package com.dnnthanh.marketplace.be.catalog.api.api;

import com.dnnthanh.marketplace.be.catalog.api.api.request.search.ProductSearchRequest;
import com.dnnthanh.marketplace.be.catalog.api.api.response.ProductResponse;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** Anonymous catalog contract. */
@RequestMapping("/products")
public interface ProductPublicApi {

    /** Searches published products with fixed deterministic ordering. */
    @GetMapping
    ApiResponse<List<ProductResponse>> search(
            @Valid @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable);

    /** Returns one published product. @param productId product id @return product details */
    @GetMapping("/{productId}")
    ProductResponse get(@PathVariable Long productId);
}

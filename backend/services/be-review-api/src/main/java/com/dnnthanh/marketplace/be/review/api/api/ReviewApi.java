package com.dnnthanh.marketplace.be.review.api.api;

import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.review.api.api.request.CreateReviewRequest;
import com.dnnthanh.marketplace.be.review.api.api.request.EditReviewRequest;
import com.dnnthanh.marketplace.be.review.api.api.request.search.ReviewSearchRequest;
import com.dnnthanh.marketplace.be.review.api.api.response.ReviewSummary;
import com.dnnthanh.marketplace.be.review.api.api.response.ReviewView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface ReviewApi {

    @GetMapping("/reviews")
    ApiResponse<List<ReviewView>> list(
            @Valid @ModelAttribute ReviewSearchRequest request,
            @PageableDefault(size = 100) Pageable pageable);

    @GetMapping("/reviews/summary")
    ReviewSummary summary(@RequestParam Long productId);

    @PostMapping("/private/reviews")
    @PreAuthorize("@authorizationService.hasPermission('REVIEW_CREATE')")
    ReviewView create(@RequestBody CreateReviewRequest request);

    @PutMapping("/private/reviews/{reviewId}")
    @PreAuthorize("@authorizationService.hasPermission('REVIEW_CREATE')")
    ReviewView edit(@PathVariable Long reviewId, @RequestBody EditReviewRequest request);

    @DeleteMapping("/private/reviews/{reviewId}")
    @PreAuthorize("@authorizationService.hasPermission('REVIEW_CREATE')")
    void delete(@PathVariable Long reviewId);

    @PostMapping("/private/reviews/{reviewId}/helpful")
    @PreAuthorize("@authorizationService.hasPermission('REVIEW_VIEW')")
    ReviewView helpful(@PathVariable Long reviewId, @RequestParam boolean active);
}

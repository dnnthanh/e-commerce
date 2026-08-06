package com.dnnthanh.marketplace.be.review.api.adapter.in.web;

import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.api.PageMetadata;
import com.dnnthanh.marketplace.be.review.api.adapter.in.web.mapper.ReviewApiMapper;
import com.dnnthanh.marketplace.be.review.api.api.ReviewApi;
import com.dnnthanh.marketplace.be.review.api.api.request.CreateReviewRequest;
import com.dnnthanh.marketplace.be.review.api.api.request.EditReviewRequest;
import com.dnnthanh.marketplace.be.review.api.api.request.search.ReviewSearchRequest;
import com.dnnthanh.marketplace.be.review.api.api.response.ReviewSummary;
import com.dnnthanh.marketplace.be.review.api.api.response.ReviewView;
import com.dnnthanh.marketplace.be.review.api.application.port.in.ReviewUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {
    private final ReviewUseCase reviews;
    private final ReviewApiMapper mapper;

    @Override
    public ApiResponse<List<ReviewView>> list(ReviewSearchRequest request, Pageable pageable) {
        Page<ReviewView> page =
                reviews.list(mapper.toCriteria(request), pageable).map(mapper::toView);
        return ApiResponse.success(page.getContent(), PageMetadata.from(page));
    }

    @Override
    public ReviewSummary summary(Long productId) {
        return mapper.toView(reviews.summary(productId));
    }

    @Override
    public ReviewView create(CreateReviewRequest request) {
        return mapper.toView(reviews.create(mapper.toCommand(request)));
    }

    @Override
    public ReviewView edit(Long reviewId, EditReviewRequest request) {
        return mapper.toView(reviews.edit(reviewId, mapper.toCommand(request)));
    }

    @Override
    public void delete(Long reviewId) {
        reviews.delete(reviewId);
    }

    @Override
    public ReviewView helpful(Long reviewId, boolean active) {
        return mapper.toView(reviews.markHelpful(reviewId, active));
    }
}

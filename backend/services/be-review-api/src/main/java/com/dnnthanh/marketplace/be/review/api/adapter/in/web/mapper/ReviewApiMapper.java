package com.dnnthanh.marketplace.be.review.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.review.api.api.request.CreateReviewRequest;
import com.dnnthanh.marketplace.be.review.api.api.request.EditReviewRequest;
import com.dnnthanh.marketplace.be.review.api.api.request.search.ReviewSearchRequest;
import com.dnnthanh.marketplace.be.review.api.api.response.ReviewSummary;
import com.dnnthanh.marketplace.be.review.api.api.response.ReviewView;
import com.dnnthanh.marketplace.be.review.api.application.command.CreateReviewCommand;
import com.dnnthanh.marketplace.be.review.api.application.command.EditReviewCommand;
import com.dnnthanh.marketplace.be.review.api.application.dto.ReviewSummaryResult;
import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSearchCriteria;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps Review HTTP transport contracts to/from application/domain objects. */
@Mapper(config = PlatformMapperConfig.class)
public interface ReviewApiMapper extends MapperContract {

    ReviewSearchCriteria toCriteria(ReviewSearchRequest request);

    CreateReviewCommand toCommand(CreateReviewRequest request);

    EditReviewCommand toCommand(EditReviewRequest request);

    @Mapping(target = "id", expression = "java(review.reviewId())")
    @Mapping(target = "productId", expression = "java(review.productId())")
    @Mapping(target = "rating", expression = "java(review.rating())")
    @Mapping(target = "title", expression = "java(review.title())")
    @Mapping(target = "content", expression = "java(review.content())")
    @Mapping(target = "createdAt", expression = "java(review.createdAt())")
    ReviewView toView(ProductReview review);

    ReviewSummary toView(ReviewSummaryResult result);
}

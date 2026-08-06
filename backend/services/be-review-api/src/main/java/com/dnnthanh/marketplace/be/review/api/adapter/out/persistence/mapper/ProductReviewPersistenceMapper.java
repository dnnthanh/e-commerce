package com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.entity.ProductReviewJpaEntity;
import com.dnnthanh.marketplace.be.review.api.domain.enumtype.ReviewStatus;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import org.mapstruct.Mapper;

/** Mapping boundary between the JPA model and the rich review aggregate. */
@Mapper(config = PlatformMapperConfig.class)
public interface ProductReviewPersistenceMapper extends MapperContract {

    default ProductReview toDomain(ProductReviewJpaEntity entity) {
        return ProductReview.rehydrate(
                entity.getId(),
                entity.getUserId(),
                entity.getProductId(),
                entity.getOrderLineId(),
                entity.getRating(),
                entity.getTitle(),
                entity.getContent(),
                entity.getCreatedAt(),
                ReviewStatus.valueOf(entity.getStatus()),
                entity.getEditVersion(),
                entity.getHelpfulUsers());
    }

    default ProductReviewJpaEntity newEntity(ProductReview review) {
        ProductReviewJpaEntity entity = new ProductReviewJpaEntity();
        copy(review, entity);
        entity.setCreatedAt(review.createdAt());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    default void copy(ProductReview review, ProductReviewJpaEntity entity) {
        entity.setUserId(review.userId());
        entity.setProductId(review.productId());
        entity.setOrderLineId(review.orderLineId());
        entity.setRating(review.rating());
        entity.setTitle(review.title());
        entity.setContent(review.content());
        entity.setStatus(review.status().getCode());
        entity.setEditVersion(review.editVersion());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setHelpfulUsers(new LinkedHashSet<>(review.helpfulUsers()));
    }
}

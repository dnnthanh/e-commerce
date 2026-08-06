package com.dnnthanh.marketplace.be.review.api.domain.model;

import com.dnnthanh.marketplace.be.review.api.domain.enumtype.ReviewStatus;
import com.dnnthanh.marketplace.be.review.api.domain.exception.InvalidReviewException;
import com.dnnthanh.marketplace.be.review.api.domain.exception.ReviewPermissionException;
import com.dnnthanh.marketplace.be.review.api.domain.exception.ReviewStateConflictException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Verified-purchase review with edit window, moderation and idempotent helpful reactions. */
public final class ProductReview {
    private final Long reviewId;
    private final String userId;
    private final Long productId;
    private final Long orderLineId;
    private final LocalDateTime createdAt;
    private final Set<String> helpfulUsers;
    private int rating;
    private String title;
    private String content;
    private ReviewStatus status;
    private int editVersion;

    public ProductReview(
            Long reviewId,
            String userId,
            Long productId,
            Long orderLineId,
            int rating,
            String title,
            String content,
            LocalDateTime createdAt) {
        this(
                reviewId,
                userId,
                productId,
                orderLineId,
                rating,
                title,
                content,
                createdAt,
                ReviewStatus.PUBLISHED,
                0,
                Set.of());
    }

    private ProductReview(
            Long reviewId,
            String userId,
            Long productId,
            Long orderLineId,
            int rating,
            String title,
            String content,
            LocalDateTime createdAt,
            ReviewStatus status,
            int editVersion,
            Set<String> helpfulUsers) {
        this.reviewId = reviewId;
        this.userId = Objects.requireNonNull(userId);
        this.productId = Objects.requireNonNull(productId);
        this.orderLineId = Objects.requireNonNull(orderLineId);
        this.createdAt = Objects.requireNonNull(createdAt);
        validateRating(rating);
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.status = Objects.requireNonNull(status);
        this.editVersion = editVersion;
        this.helpfulUsers = new HashSet<>(helpfulUsers);
    }

    public static ProductReview rehydrate(
            Long reviewId,
            String userId,
            Long productId,
            Long orderLineId,
            int rating,
            String title,
            String content,
            LocalDateTime createdAt,
            ReviewStatus status,
            int editVersion,
            Set<String> helpfulUsers) {
        return new ProductReview(
                reviewId,
                userId,
                productId,
                orderLineId,
                rating,
                title,
                content,
                createdAt,
                status,
                editVersion,
                helpfulUsers);
    }

    public void edit(
            String actorId,
            int rating,
            String title,
            String content,
            LocalDateTime at,
            Duration editWindow) {
        if (!userId.equals(actorId)) {
            throw new ReviewPermissionException("Only review owner can edit");
        }
        if (Duration.between(createdAt, at).compareTo(editWindow) > 0) {
            throw new ReviewStateConflictException("Review edit window expired");
        }
        if (status == ReviewStatus.DELETED) {
            throw new ReviewStateConflictException("Deleted review cannot be edited");
        }
        validateRating(rating);
        this.rating = rating;
        this.title = title;
        this.content = content;
        editVersion++;
    }

    public boolean markHelpful(String actorId) {
        return helpfulUsers.add(actorId);
    }

    public boolean removeHelpful(String actorId) {
        return helpfulUsers.remove(actorId);
    }

    public void requireModeration() {
        if (status == ReviewStatus.PUBLISHED) {
            status = ReviewStatus.MODERATION_REQUIRED;
        }
    }

    public void hide() {
        if (status == ReviewStatus.DELETED) {
            throw new ReviewStateConflictException("Deleted review cannot be moderated");
        }
        status = ReviewStatus.HIDDEN;
    }

    public void restore() {
        if (status == ReviewStatus.DELETED) {
            throw new ReviewStateConflictException(
                    "Deleted review cannot be restored by moderation");
        }
        status = ReviewStatus.PUBLISHED;
    }

    public void delete(String actorId) {
        if (!userId.equals(actorId)) {
            throw new ReviewPermissionException("Only review owner can delete");
        }
        status = ReviewStatus.DELETED;
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new InvalidReviewException("Rating must be 1..5");
        }
    }

    public Long reviewId() {
        return reviewId;
    }

    public String userId() {
        return userId;
    }

    public Long productId() {
        return productId;
    }

    public Long orderLineId() {
        return orderLineId;
    }

    public int rating() {
        return rating;
    }

    public String title() {
        return title;
    }

    public String content() {
        return content;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public ReviewStatus status() {
        return status;
    }

    public Set<String> helpfulUsers() {
        return Set.copyOf(helpfulUsers);
    }

    public int helpfulCount() {
        return helpfulUsers.size();
    }

    public int editVersion() {
        return editVersion;
    }
}

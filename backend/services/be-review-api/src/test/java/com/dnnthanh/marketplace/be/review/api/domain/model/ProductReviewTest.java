package com.dnnthanh.marketplace.be.review.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.review.api.domain.exception.ReviewPermissionException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ProductReviewTest {
    @Test
    void protectsOwnershipAndHelpfulReactionIdempotency() {
        ProductReview review =
                new ProductReview(1L, "U1", 2L, 3L, 5, "t", "c", LocalDateTime.now());
        assertTrue(review.markHelpful("U2"));
        assertFalse(review.markHelpful("U2"));
        assertThrows(
                ReviewPermissionException.class,
                () -> review.edit("U2", 4, "x", "y", LocalDateTime.now(), Duration.ofDays(1)));
    }
}

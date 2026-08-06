package com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Relational persistence model for the review aggregate. */
@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class ProductReviewJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_line_id", nullable = false, unique = true)
    private Long orderLineId;

    @Column(nullable = false)
    private int rating;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "edit_version", nullable = false)
    private int editVersion;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_helpful", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "user_id", nullable = false, length = 64)
    private Set<String> helpfulUsers = new LinkedHashSet<>();
}

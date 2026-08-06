package com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Relational promotion row used as the Spring Data repository root. */
@Entity
@Table(name = "promotion")
@Getter
@NoArgsConstructor
public class PromotionJpaEntity {
    @Id private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "promotion_type", nullable = false)
    private String promotionType;

    @Column(name = "stacking_group")
    private String stackingGroup;

    @Column(nullable = false)
    private Integer priority;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private String status;
}

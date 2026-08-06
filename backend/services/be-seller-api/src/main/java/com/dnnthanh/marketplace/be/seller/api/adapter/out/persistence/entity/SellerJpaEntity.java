package com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA model for seller lifecycle state. */
@Entity
@Table(name = "seller")
@Getter
@Setter
@NoArgsConstructor
public class SellerJpaEntity {
    @Id private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

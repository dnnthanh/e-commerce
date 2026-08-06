package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA model for the durable media metadata lifecycle. */
@Entity
@Table(name = "media_asset")
@Getter
@Setter
@NoArgsConstructor
public class MediaMetadataJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "original_object_key", nullable = false, unique = true, length = 512)
    private String originalObjectKey;

    @Column(name = "media_type", nullable = false, length = 32)
    private String mediaType;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;
}

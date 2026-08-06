package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.media.api.domain.enumtype.MediaStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA source-of-truth entity for media processing state. */
@Entity
@Table(name = "media_processing_asset")
@Getter
@Setter
@NoArgsConstructor()
public class MediaProcessingAssetJpaEntity {
    @Id
    @Column(name = "media_id")
    private Long mediaId;

    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    @Column(nullable = false, unique = true, length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MediaStatus status;

    @Column(name = "reference_count", nullable = false)
    private int referenceCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "media_processing_variant",
            joinColumns = @JoinColumn(name = "media_id"))
    @Column(name = "variant_key", nullable = false, length = 256)
    private Set<String> variants = new LinkedHashSet<>();
}

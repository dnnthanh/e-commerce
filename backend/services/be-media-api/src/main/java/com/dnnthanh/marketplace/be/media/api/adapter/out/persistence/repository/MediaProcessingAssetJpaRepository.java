package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.entity.MediaProcessingAssetJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for ordinary media-processing aggregate persistence. */
public interface MediaProcessingAssetJpaRepository
        extends JpaRepository<MediaProcessingAssetJpaEntity, Long> {

    @EntityGraph(attributePaths = "variants")
    Optional<MediaProcessingAssetJpaEntity> findWithVariantsByMediaId(Long mediaId);

    @EntityGraph(attributePaths = "variants")
    Optional<MediaProcessingAssetJpaEntity> findWithVariantsByChecksum(String checksum);
}

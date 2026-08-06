package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.entity.MediaMetadataJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Media metadata repository. Variant lookup is deliberately native SQL because it is a read
 * projection.
 */
public interface MediaMetadataJpaRepository extends JpaRepository<MediaMetadataJpaEntity, Long> {
    @Query(
            value =
                    """
          SELECT mv.placement AS placement,
                 mv.width_px AS width,
                 mv.height_px AS height,
                 mv.format AS format,
                 mv.object_key AS objectKey
          FROM media_variant mv
          JOIN media_asset ma ON ma.id = mv.asset_id
          WHERE ma.product_id = :productId
          ORDER BY mv.width_px, mv.id
          """,
            nativeQuery = true)
    List<VariantProjection> findVariantsNative(@Param("productId") Long productId);

    interface VariantProjection {
        String getPlacement();

        int getWidth();

        int getHeight();

        String getFormat();

        String getObjectKey();
    }
}

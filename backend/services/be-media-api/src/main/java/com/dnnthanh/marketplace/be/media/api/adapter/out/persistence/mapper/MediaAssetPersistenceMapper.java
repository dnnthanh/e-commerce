package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.entity.MediaProcessingAssetJpaEntity;
import com.dnnthanh.marketplace.be.media.api.domain.model.MediaAsset;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import org.mapstruct.Mapper;

/** MapStruct-owned persistence boundary for media process state. */
@Mapper(config = PlatformMapperConfig.class)
public interface MediaAssetPersistenceMapper extends MapperContract {

    default MediaAsset toDomain(MediaProcessingAssetJpaEntity entity) {
        return MediaAsset.rehydrate(
                String.valueOf(entity.getMediaId()),
                entity.getOwnerId(),
                entity.getChecksum(),
                entity.getVariants(),
                entity.getStatus(),
                entity.getReferenceCount());
    }

    default MediaProcessingAssetJpaEntity toNewEntity(MediaAsset asset) {
        MediaProcessingAssetJpaEntity entity = new MediaProcessingAssetJpaEntity();
        synchronize(asset, entity);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    default void synchronize(MediaAsset asset, MediaProcessingAssetJpaEntity entity) {
        entity.setMediaId(Long.valueOf(asset.mediaId()));
        entity.setOwnerId(asset.ownerId());
        entity.setChecksum(asset.checksum());
        entity.setStatus(asset.status());
        entity.setReferenceCount(asset.referenceCount());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVariants(new LinkedHashSet<>(asset.generatedVariants()));
    }
}

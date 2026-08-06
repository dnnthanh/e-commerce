package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.entity.MediaProcessingAssetJpaEntity;
import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.mapper.MediaAssetPersistenceMapper;
import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.repository.MediaProcessingAssetJpaRepository;
import com.dnnthanh.marketplace.be.media.api.application.port.out.MediaAssetPort;
import com.dnnthanh.marketplace.be.media.api.domain.model.MediaAsset;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Ordinary media processing CRUD uses Spring Data JPA instead of raw JDBC. */
@Persistence
@RequiredArgsConstructor
public class MediaAssetPersistenceAdapter implements MediaAssetPort {
    private final MediaProcessingAssetJpaRepository repository;
    private final MediaAssetPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaAsset> findById(String mediaId) {
        return repository.findWithVariantsByMediaId(Long.valueOf(mediaId)).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaAsset> findByChecksum(String checksum) {
        return repository.findWithVariantsByChecksum(checksum).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public MediaAsset save(MediaAsset asset) {
        MediaProcessingAssetJpaEntity entity =
                repository
                        .findWithVariantsByMediaId(Long.valueOf(asset.mediaId()))
                        .orElseGet(() -> mapper.toNewEntity(asset));
        mapper.synchronize(asset, entity);
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}

package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.entity.MediaMetadataJpaEntity;
import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.exception.MediaMetadataPersistenceException;
import com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.repository.MediaMetadataJpaRepository;
import com.dnnthanh.marketplace.be.media.api.application.port.out.MediaMetadataPort;
import com.dnnthanh.marketplace.be.media.api.domain.model.ImagePlacement;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * Media metadata persistence.
 *
 * <p>Lifecycle CRUD uses JPA with optimistic locking. Variant search is native SQL through Spring
 * Data. JdbcClient remains only for the PostgreSQL JSONB transactional-outbox insert.
 */
@Persistence
@RequiredArgsConstructor
public class MediaMetadataPersistenceAdapter implements MediaMetadataPort {
    private final MediaMetadataJpaRepository media;
    private final JdbcClient jdbc;

    @Override
    @Transactional
    public Long createUploadingAsset(
            Long productId, Long sellerId, String objectKey, String contentType) {
        LocalDateTime now = LocalDateTime.now();
        MediaMetadataJpaEntity entity = new MediaMetadataJpaEntity();
        entity.setProductId(productId);
        entity.setSellerId(sellerId);
        entity.setOriginalObjectKey(objectKey);
        entity.setMediaType("IMAGE");
        entity.setStatus("UPLOADING");
        entity.setContentType(contentType);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return media.saveAndFlush(entity).getId();
    }

    @Override
    @Transactional
    public void markUploaded(Long assetId) {
        MediaMetadataJpaEntity entity =
                media.findById(assetId)
                        .orElseThrow(
                                () ->
                                        new MediaMetadataPersistenceException(
                                                "Media asset not found: " + assetId));
        if (!"UPLOADING".equals(entity.getStatus())) {
            throw new MediaMetadataPersistenceException(
                    "Media asset cannot transition to UPLOADED from " + entity.getStatus());
        }
        entity.setStatus("UPLOADED");
        entity.setUpdatedAt(LocalDateTime.now());
        media.saveAndFlush(entity);
        int changed =
                jdbc.sql(
                                """
                INSERT INTO outbox_event(
                    event_id,aggregate_id,event_type,payload_json,status,created_at)
                VALUES(
                    :eventId,:aggregateId,'MEDIA_UPLOADED',
                    jsonb_build_object('assetId',:assetId),'PENDING',now())
                """)
                        .param("eventId", UUID.randomUUID().toString())
                        .param("aggregateId", assetId.toString())
                        .param("assetId", assetId)
                        .update();
        if (changed != 1) {
            throw new MediaMetadataPersistenceException("Media outbox event was not persisted");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantMetadata> findVariants(Long productId) {
        return media.findVariantsNative(productId).stream()
                .map(
                        value ->
                                new VariantMetadata(
                                        ImagePlacement.valueOf(value.getPlacement()),
                                        value.getWidth(),
                                        value.getHeight(),
                                        value.getFormat(),
                                        value.getObjectKey()))
                .toList();
    }
}

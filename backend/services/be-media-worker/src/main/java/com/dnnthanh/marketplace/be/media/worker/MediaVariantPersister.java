package com.dnnthanh.marketplace.be.media.worker;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

@Persistence
@RequiredArgsConstructor
public class MediaVariantPersister {

    private final JdbcClient jdbc;

    @Transactional
    public void markReady(
            long assetId, long productId, List<MediaVariantWorker.GeneratedVariant> variants) {
        for (MediaVariantWorker.GeneratedVariant variant : variants) {
            jdbc.sql(
                            """
                            INSERT INTO media_variant(
                                asset_id, placement, width_px, height_px,
                                format, object_key, byte_size, created_at)
                            VALUES(
                                :asset, :placement, :width, :height,
                                :format, :key, :size, :created)
                            ON CONFLICT DO NOTHING
                            """)
                    .param("asset", assetId)
                    .param("placement", variant.placement())
                    .param("width", variant.width())
                    .param("height", variant.height())
                    .param("format", variant.format())
                    .param("key", variant.objectKey())
                    .param("size", variant.byteSize())
                    .param("created", LocalDateTime.now())
                    .update();
        }

        jdbc.sql("UPDATE media_asset SET status='READY', updated_at=now() WHERE id=:id")
                .param("id", assetId)
                .update();

        jdbc.sql(
                        """
                        INSERT INTO outbox_event(
                            event_id, aggregate_id, event_type, payload_json, status, created_at)
                        VALUES(
                            :event, :aggregate, 'MEDIA_VARIANTS_READY',
                            jsonb_build_object('assetId', :asset, 'productId', :product),
                            'PENDING', now())
                        """)
                .param("event", UUID.randomUUID().toString())
                .param("aggregate", String.valueOf(productId))
                .param("asset", assetId)
                .param("product", productId)
                .update();
    }
}

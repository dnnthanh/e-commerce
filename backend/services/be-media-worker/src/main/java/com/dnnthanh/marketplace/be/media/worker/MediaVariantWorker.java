package com.dnnthanh.marketplace.be.media.worker;

import com.dnnthanh.marketplace.be.media.worker.config.MediaWorkerStorageProperties;
import com.dnnthanh.marketplace.be.media.worker.exception.MediaVariantProcessingException;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class MediaVariantWorker extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;
    private final RestClient imgproxyRestClient;
    private final MinioClient mediaWorkerMinioClient;
    private final MediaVariantPersister persister;
    private final MediaWorkerStorageProperties storageProperties;

    @KafkaListener(topics = "marketplace.media.events", groupId = "be-media-worker-v2")
    public void process(DomainEvent event) {
        if (!accepts(event, "MEDIA_UPLOADED")) {
            return;
        }

        long assetId = Long.parseLong(String.valueOf(event.payload().get("assetId")));
        Map<String, Object> asset =
                jdbc.sql("SELECT original_object_key, product_id FROM media_asset WHERE id=:id")
                        .param("id", assetId)
                        .query()
                        .singleRow();
        String originalKey = String.valueOf(asset.get("original_object_key"));
        long productId = Long.parseLong(String.valueOf(asset.get("product_id")));
        List<GeneratedVariant> generated = new ArrayList<>();

        try {
            String originalUrl =
                    mediaWorkerMinioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(storageProperties.getBucket())
                                    .object(originalKey)
                                    .expiry(15, TimeUnit.MINUTES)
                                    .build());
            String encodedSource = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);

            for (VariantSpec spec : VariantSpec.storefront()) {
                byte[] body =
                        imgproxyRestClient
                                .get()
                                .uri(
                                        "/unsafe/rs:fit:{w}:{h}:0/plain/{source}@{format}",
                                        spec.width(),
                                        spec.height(),
                                        encodedSource,
                                        spec.format())
                                .retrieve()
                                .body(byte[].class);
                if (body == null || body.length == 0) {
                    throw new MediaVariantProcessingException("imgproxy returned an empty variant");
                }

                String key =
                        "variants/"
                                + productId
                                + "/"
                                + assetId
                                + "/"
                                + spec.placement().toLowerCase()
                                + "-"
                                + spec.width()
                                + "."
                                + spec.format();
                mediaWorkerMinioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(key)
                                .stream(new ByteArrayInputStream(body), body.length, -1)
                                .contentType("image/" + spec.format())
                                .build());
                generated.add(
                        new GeneratedVariant(
                                spec.placement(),
                                spec.width(),
                                spec.height(),
                                spec.format(),
                                key,
                                body.length));
            }
        } catch (MediaVariantProcessingException known) {
            throw known;
        } catch (Exception failure) {
            throw new MediaVariantProcessingException(
                    "Media variant generation failed for asset " + assetId, failure);
        }

        // Network and object-storage I/O finish before the local database transaction starts.
        persister.markReady(assetId, productId, generated);
    }

    record VariantSpec(String placement, int width, int height, String format) {
        static List<VariantSpec> storefront() {
            return List.of(
                    new VariantSpec("HOME_CARD", 480, 480, "avif"),
                    new VariantSpec("SEARCH_CARD", 640, 640, "avif"),
                    new VariantSpec("PRODUCT_DETAIL", 1600, 1600, "webp"));
        }
    }

    record GeneratedVariant(
            String placement,
            int width,
            int height,
            String format,
            String objectKey,
            long byteSize) {}
}

package com.dnnthanh.marketplace.be.media.api.application.port.out;

import com.dnnthanh.marketplace.be.media.api.domain.model.ImagePlacement;
import java.util.List;

/** Media metadata/outbox persistence boundary. */
public interface MediaMetadataPort {
    Long createUploadingAsset(Long productId, Long sellerId, String objectKey, String contentType);

    void markUploaded(Long assetId);

    List<VariantMetadata> findVariants(Long productId);

    record VariantMetadata(
            ImagePlacement placement, int width, int height, String format, String objectKey) {}
}

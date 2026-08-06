package com.dnnthanh.marketplace.be.media.api.application.service;

import com.dnnthanh.marketplace.be.media.api.application.command.CreateMediaUploadCommand;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaReadinessResult;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaUploadSessionResult;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaVariantResult;
import com.dnnthanh.marketplace.be.media.api.application.port.in.MediaUseCase;
import com.dnnthanh.marketplace.be.media.api.application.port.out.MediaAssetPort;
import com.dnnthanh.marketplace.be.media.api.application.port.out.MediaMetadataPort;
import com.dnnthanh.marketplace.be.media.api.application.port.out.MediaObjectStoragePort;
import com.dnnthanh.marketplace.be.media.api.domain.exception.MediaStateConflictException;
import com.dnnthanh.marketplace.be.media.api.domain.model.ImagePlacement;
import com.dnnthanh.marketplace.be.media.api.domain.model.MediaAsset;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** Media upload/metadata use cases with checksum duplicate protection. */
@UseCase
@RequiredArgsConstructor
public class MediaServiceImplement implements MediaUseCase {
    private static final int UPLOAD_EXPIRY_MINUTES = 15;
    private static final Set<ImagePlacement> REQUIRED_PLACEMENTS =
            Set.of(
                    ImagePlacement.HOME_CARD,
                    ImagePlacement.SEARCH_CARD,
                    ImagePlacement.PRODUCT_DETAIL);

    private final MediaMetadataPort metadata;
    private final MediaObjectStoragePort objectStorage;
    private final MediaAssetPort mediaAssetPort;

    @Override
    public MediaUploadSessionResult create(CreateMediaUploadCommand command) {
        mediaAssetPort
                .findByChecksum(command.checksum())
                .ifPresent(
                        duplicate -> {
                            throw new MediaStateConflictException(
                                    "Media checksum already exists: " + duplicate.mediaId());
                        });
        String objectKey =
                "products/"
                        + command.productId()
                        + "/original/"
                        + UUID.randomUUID()
                        + "-"
                        + sanitize(command.filename());
        Long assetId =
                metadata.createUploadingAsset(
                        command.productId(), command.sellerId(), objectKey, command.contentType());
        mediaAssetPort.save(
                new MediaAsset(
                        assetId.toString(), command.sellerId().toString(), command.checksum()));
        String uploadUrl = objectStorage.presignUpload(objectKey, UPLOAD_EXPIRY_MINUTES);
        return new MediaUploadSessionResult(
                assetId,
                objectKey,
                uploadUrl,
                LocalDateTime.now().plusMinutes(UPLOAD_EXPIRY_MINUTES));
    }

    @Override
    public void complete(Long assetId) {
        metadata.markUploaded(assetId);
        MediaAsset asset =
                mediaAssetPort
                        .findById(assetId.toString())
                        .orElseThrow(
                                () ->
                                        new MediaStateConflictException(
                                                "Media process state not found: " + assetId));
        asset.uploaded();
        mediaAssetPort.save(asset);
    }

    @Override
    public List<MediaVariantResult> variants(Long productId) {
        return metadata.findVariants(productId).stream()
                .map(
                        value ->
                                new MediaVariantResult(
                                        value.placement(),
                                        value.width(),
                                        value.height(),
                                        value.format(),
                                        value.objectKey()))
                .toList();
    }

    @Override
    public MediaReadinessResult readiness(Long productId) {
        Set<ImagePlacement> found = new HashSet<>();
        metadata.findVariants(productId).forEach(value -> found.add(value.placement()));
        return new MediaReadinessResult(found.containsAll(REQUIRED_PLACEMENTS));
    }

    private static String sanitize(String filename) {
        return filename.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}

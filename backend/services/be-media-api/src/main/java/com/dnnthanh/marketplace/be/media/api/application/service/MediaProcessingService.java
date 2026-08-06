package com.dnnthanh.marketplace.be.media.api.application.service;

import com.dnnthanh.marketplace.be.media.api.application.port.out.MediaAssetPort;
import com.dnnthanh.marketplace.be.media.api.domain.exception.MediaStateConflictException;
import com.dnnthanh.marketplace.be.media.api.domain.model.MediaAsset;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;

/** Media processing state machine used by asynchronous scanning/transformation workers. */
@UseCase
@RequiredArgsConstructor
public class MediaProcessingService {
    private final MediaAssetPort repository;

    public MediaAsset startScan(String mediaId) {
        MediaAsset asset = load(mediaId);
        asset.scanning();
        return repository.save(asset);
    }

    public MediaAsset scanPassed(String mediaId) {
        MediaAsset asset = load(mediaId);
        asset.scanPassed();
        return repository.save(asset);
    }

    public MediaAsset scanFailed(String mediaId) {
        MediaAsset asset = load(mediaId);
        asset.scanFailed();
        return repository.save(asset);
    }

    public boolean markVariantGenerated(String mediaId, String variantKey) {
        MediaAsset asset = load(mediaId);
        boolean newlyGenerated = asset.variantGenerated(variantKey);
        if (newlyGenerated) {
            repository.save(asset);
        }
        return newlyGenerated;
    }

    public MediaAsset markReady(String mediaId) {
        MediaAsset asset = load(mediaId);
        asset.ready();
        return repository.save(asset);
    }

    public MediaAsset requestDelete(String mediaId) {
        MediaAsset asset = load(mediaId);
        asset.requestDelete();
        return repository.save(asset);
    }

    private MediaAsset load(String mediaId) {
        return repository
                .findById(mediaId)
                .orElseThrow(() -> new MediaStateConflictException("Media not found: " + mediaId));
    }
}

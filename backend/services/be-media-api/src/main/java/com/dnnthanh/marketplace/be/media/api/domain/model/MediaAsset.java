package com.dnnthanh.marketplace.be.media.api.domain.model;

import com.dnnthanh.marketplace.be.media.api.domain.enumtype.MediaStatus;
import com.dnnthanh.marketplace.be.media.api.domain.exception.MediaStateConflictException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Media processing aggregate with checksum idempotency and safe delayed deletion semantics. */
public final class MediaAsset {
    private final String mediaId;
    private final String ownerId;
    private final String checksum;
    private final Set<String> generatedVariants;
    private MediaStatus status;
    private int referenceCount;

    public MediaAsset(String mediaId, String ownerId, String checksum) {
        this(mediaId, ownerId, checksum, Set.of(), MediaStatus.INITIATED, 0);
    }

    private MediaAsset(
            String mediaId,
            String ownerId,
            String checksum,
            Set<String> generatedVariants,
            MediaStatus status,
            int referenceCount) {
        this.mediaId = Objects.requireNonNull(mediaId);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.checksum = Objects.requireNonNull(checksum);
        this.generatedVariants = new HashSet<>(generatedVariants);
        this.status = Objects.requireNonNull(status);
        this.referenceCount = referenceCount;
    }

    public static MediaAsset rehydrate(
            String mediaId,
            String ownerId,
            String checksum,
            Set<String> generatedVariants,
            MediaStatus status,
            int referenceCount) {
        return new MediaAsset(
                mediaId, ownerId, checksum, generatedVariants, status, referenceCount);
    }

    public void uploaded() {
        require(MediaStatus.INITIATED);
        status = MediaStatus.UPLOADED;
    }

    public void scanning() {
        require(MediaStatus.UPLOADED);
        status = MediaStatus.SCANNING;
    }

    public void scanPassed() {
        require(MediaStatus.SCANNING);
        status = MediaStatus.PROCESSING;
    }

    public void scanFailed() {
        require(MediaStatus.SCANNING);
        status = MediaStatus.FAILED;
    }

    public boolean variantGenerated(String variantKey) {
        if (status != MediaStatus.PROCESSING) {
            throw new MediaStateConflictException(
                    "Variants can only be generated while processing");
        }
        return generatedVariants.add(Objects.requireNonNull(variantKey));
    }

    public void ready() {
        require(MediaStatus.PROCESSING);
        status = MediaStatus.READY;
    }

    public void attach() {
        if (status != MediaStatus.READY) {
            throw new MediaStateConflictException("Only ready media can be attached");
        }
        referenceCount++;
    }

    public void detach() {
        if (referenceCount > 0) {
            referenceCount--;
        }
    }

    public void requestDelete() {
        if (referenceCount > 0) {
            throw new MediaStateConflictException("Referenced media cannot be deleted");
        }
        if (status == MediaStatus.DELETED) {
            return;
        }
        status = MediaStatus.DELETE_PENDING;
    }

    public void deleted() {
        require(MediaStatus.DELETE_PENDING);
        status = MediaStatus.DELETED;
    }

    private void require(MediaStatus expected) {
        if (status != expected) {
            throw new MediaStateConflictException(
                    "Media state conflict: expected=" + expected + ", actual=" + status);
        }
    }

    public String mediaId() {
        return mediaId;
    }

    public String ownerId() {
        return ownerId;
    }

    public String checksum() {
        return checksum;
    }

    public Set<String> generatedVariants() {
        return Set.copyOf(generatedVariants);
    }

    public MediaStatus status() {
        return status;
    }

    public int referenceCount() {
        return referenceCount;
    }
}

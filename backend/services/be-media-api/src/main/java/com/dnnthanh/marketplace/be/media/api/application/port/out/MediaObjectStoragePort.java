package com.dnnthanh.marketplace.be.media.api.application.port.out;

/** Object-storage anti-corruption port. */
public interface MediaObjectStoragePort {
    String presignUpload(String objectKey, int expiryMinutes);
}

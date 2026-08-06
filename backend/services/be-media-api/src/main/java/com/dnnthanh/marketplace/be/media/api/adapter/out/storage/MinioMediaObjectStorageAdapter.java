package com.dnnthanh.marketplace.be.media.api.adapter.out.storage;

import com.dnnthanh.marketplace.be.media.api.adapter.out.storage.exception.MediaStorageException;
import com.dnnthanh.marketplace.be.media.api.application.port.out.MediaObjectStoragePort;
import com.dnnthanh.marketplace.be.media.api.config.MinioProperties;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class MinioMediaObjectStorageAdapter implements MediaObjectStoragePort {

    private final MinioClient minio;
    private final MinioProperties properties;

    @Override
    public String presignUpload(String objectKey, int expiryMinutes) {
        try {
            return minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception failure) {
            throw new MediaStorageException("Unable to create presigned upload URL", failure);
        }
    }
}

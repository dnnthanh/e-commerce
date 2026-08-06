package com.dnnthanh.marketplace.be.media.api.application.port.in;

import com.dnnthanh.marketplace.be.media.api.application.command.CreateMediaUploadCommand;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaReadinessResult;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaUploadSessionResult;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaVariantResult;
import java.util.List;

/** Inbound application port for media upload and product-media readiness. */
public interface MediaUseCase {
    MediaUploadSessionResult create(CreateMediaUploadCommand command);

    void complete(Long assetId);

    List<MediaVariantResult> variants(Long productId);

    MediaReadinessResult readiness(Long productId);
}

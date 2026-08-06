package com.dnnthanh.marketplace.be.media.api.api;

import com.dnnthanh.marketplace.be.media.api.api.request.CreateUploadRequest;
import com.dnnthanh.marketplace.be.media.api.api.response.Readiness;
import com.dnnthanh.marketplace.be.media.api.api.response.UploadSession;
import com.dnnthanh.marketplace.be.media.api.api.response.VariantView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Media upload, variant discovery and internal readiness contract. */
public interface MediaApi {
    @PostMapping("/private/media/uploads")
    @PreAuthorize("@authorizationService.hasSellerPermission('MEDIA_UPLOAD', #request.sellerId())")
    UploadSession createUpload(@Valid @RequestBody CreateUploadRequest request);

    @PostMapping("/private/media/uploads/{assetId}/complete")
    @PreAuthorize("@authorizationService.hasPermission('MEDIA_UPLOAD')")
    void complete(@PathVariable Long assetId);

    @GetMapping("/media/products/{productId}/assets")
    List<VariantView> productAssets(@PathVariable Long productId);

    @GetMapping("/internal/media/products/{productId}/readiness")
    Readiness readiness(@PathVariable Long productId);

    /** Upload metadata including a client-computed SHA-256 used for duplicate detection. */
}

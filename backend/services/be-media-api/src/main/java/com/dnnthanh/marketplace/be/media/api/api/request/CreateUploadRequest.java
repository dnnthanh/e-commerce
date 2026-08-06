package com.dnnthanh.marketplace.be.media.api.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUploadRequest(
        @NotNull Long productId,
        @NotNull Long sellerId,
        @NotBlank String contentType,
        @NotBlank String filename,
        @NotBlank String checksum) {}

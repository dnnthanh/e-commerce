package com.dnnthanh.marketplace.be.media.api.application.dto;

import java.time.LocalDateTime;

/** Upload session returned by the media application boundary. */
public record MediaUploadSessionResult(
        Long assetId, String objectKey, String uploadUrl, LocalDateTime expiresAt) {}

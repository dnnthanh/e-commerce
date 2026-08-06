package com.dnnthanh.marketplace.be.media.api.api.response;

import java.time.LocalDateTime;

public record UploadSession(
        Long assetId, String objectKey, String uploadUrl, LocalDateTime expiresAt) {}

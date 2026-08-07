package com.dnnthanh.marketplace.be.media.api.api.response;

import com.dnnthanh.marketplace.be.media.api.domain.model.ImagePlacement;

public record VariantView(
        ImagePlacement placement,
        int width,
        int height,
        String format,
        String objectKey,
        String url) {}

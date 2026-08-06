package com.dnnthanh.marketplace.be.media.api.application.dto;

import com.dnnthanh.marketplace.be.media.api.domain.model.ImagePlacement;

/** Media variant application read model. */
public record MediaVariantResult(
        ImagePlacement placement, int width, int height, String format, String objectKey) {}

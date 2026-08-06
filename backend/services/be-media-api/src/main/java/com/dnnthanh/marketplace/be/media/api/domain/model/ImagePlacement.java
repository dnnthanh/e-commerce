package com.dnnthanh.marketplace.be.media.api.domain.model;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Storefront image placements and target dimensions. */
public enum ImagePlacement implements CodeEnum {
    HOME_CARD(480, 480, "avif"),
    SEARCH_CARD(640, 640, "avif"),
    PRODUCT_DETAIL(1600, 1600, "webp"),
    THUMBNAIL(160, 160, "avif");

    private final int width;

    private final int height;

    private final String format;

    ImagePlacement(int width, int height, String format) {
        this.width = width;
        this.height = height;
        this.format = format;
    }

    /**
     * @return width
     */
    public int width() {
        return width;
    }

    /**
     * @return height
     */
    public int height() {
        return height;
    }

    /**
     * @return format
     */
    public String format() {
        return format;
    }
}

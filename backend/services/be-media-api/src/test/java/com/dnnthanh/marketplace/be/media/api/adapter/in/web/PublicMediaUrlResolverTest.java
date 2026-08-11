package com.dnnthanh.marketplace.be.media.api.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dnnthanh.marketplace.be.media.api.config.MinioProperties;
import org.junit.jupiter.api.Test;

class PublicMediaUrlResolverTest {
    @Test
    void resolvesBrowserVisibleObjectUrlFromPublicBaseUrlAndBucket() {
        MinioProperties properties =
                new MinioProperties(
                        "http://minio:9000",
                        "http://localhost:9000/",
                        "minioadmin",
                        "minioadmin",
                        "marketplace-media");

        PublicMediaUrlResolver resolver = new PublicMediaUrlResolver(properties);

        assertEquals(
                "http://localhost:9000/marketplace-media/seed/products/1001/thumbnail.webp",
                resolver.resolve("/seed/products/1001/thumbnail.webp"));
    }
}

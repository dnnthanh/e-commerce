package com.dnnthanh.marketplace.be.media.api.adapter.in.web;

import com.dnnthanh.marketplace.be.media.api.config.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Builds browser-visible media URLs without leaking the internal Docker MinIO endpoint. */
@Component
@RequiredArgsConstructor
public class PublicMediaUrlResolver {
    private final MinioProperties properties;

    public String resolve(String objectKey) {
        String baseUrl = stripTrailingSlash(properties.getPublicBaseUrl());
        String bucket = stripSlashes(properties.getBucket());
        String key = stripLeadingSlash(objectKey);
        return baseUrl + "/" + bucket + "/" + key;
    }

    private String stripTrailingSlash(String value) {
        String normalized = value;
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String stripLeadingSlash(String value) {
        String normalized = value;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private String stripSlashes(String value) {
        return stripLeadingSlash(stripTrailingSlash(value));
    }
}

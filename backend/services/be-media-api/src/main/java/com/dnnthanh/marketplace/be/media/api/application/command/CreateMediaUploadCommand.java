package com.dnnthanh.marketplace.be.media.api.application.command;

/** Application command for initiating a media upload. */
public record CreateMediaUploadCommand(
        Long productId, Long sellerId, String contentType, String filename, String checksum) {}

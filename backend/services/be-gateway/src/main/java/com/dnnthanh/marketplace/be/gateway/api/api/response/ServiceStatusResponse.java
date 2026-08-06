package com.dnnthanh.marketplace.be.gateway.api.api.response;

import java.time.LocalDateTime;

/**
 * Service status response.
 *
 * @param service bounded-context service name
 * @param capability primary business capability
 * @param timestamp response timestamp
 * @param authenticated whether response requires user authentication
 */
public record ServiceStatusResponse(
        String service, String capability, LocalDateTime timestamp, boolean authenticated) {}

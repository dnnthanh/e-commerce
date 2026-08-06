package com.dnnthanh.marketplace.be.notification.api.application.query;

/** User-facing channel preferences used by the inbox API. */
public record NotificationPreferenceSnapshot(
        boolean realtime, boolean email, boolean push, boolean sellerUpdates) {}

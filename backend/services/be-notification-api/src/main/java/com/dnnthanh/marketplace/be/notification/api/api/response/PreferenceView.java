package com.dnnthanh.marketplace.be.notification.api.api.response;

public record PreferenceView(
        boolean realtime, boolean email, boolean push, boolean sellerUpdates) {}

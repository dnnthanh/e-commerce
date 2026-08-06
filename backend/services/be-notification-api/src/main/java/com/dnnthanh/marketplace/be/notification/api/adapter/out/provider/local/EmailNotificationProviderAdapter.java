package com.dnnthanh.marketplace.be.notification.api.adapter.out.provider.local;

import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationProviderPort;
import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;

/** Local Email provider adapter used by the demo environment. */
@Adapter
public class EmailNotificationProviderAdapter implements NotificationProviderPort {
    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public DeliveryResult send(NotificationDelivery delivery, String attemptKey) {
        return DeliveryResult.delivered();
    }
}

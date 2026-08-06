package com.dnnthanh.marketplace.be.notification.api.application.port.in;

import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationInboxItem;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationPreferenceSnapshot;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationSearchCriteria;
import java.util.List;

/** Inbound application port for the current user's durable notification inbox. */
public interface NotificationInboxUseCase {
    List<NotificationInboxItem> list(NotificationSearchCriteria criteria);

    long unreadCount();

    void markRead(String notificationId);

    void markAllRead();

    NotificationPreferenceSnapshot preference();

    NotificationPreferenceSnapshot savePreference(NotificationPreferenceSnapshot preference);

    void setSellerFollow(Long sellerId, boolean active);
}

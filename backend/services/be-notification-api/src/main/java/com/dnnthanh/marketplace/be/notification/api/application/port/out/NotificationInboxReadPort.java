package com.dnnthanh.marketplace.be.notification.api.application.port.out;

import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationInboxItem;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationPreferenceSnapshot;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationSearchCriteria;
import java.util.List;

/** Durable inbox query/update boundary; controllers never access Mongo directly. */
public interface NotificationInboxReadPort {
    List<NotificationInboxItem> list(String userId, NotificationSearchCriteria criteria);

    long unreadCount(String userId);

    void markRead(String userId, String notificationId);

    void markAllRead(String userId);

    NotificationPreferenceSnapshot preferenceSnapshot(String userId);

    NotificationPreferenceSnapshot savePreference(
            String userId, NotificationPreferenceSnapshot preference);

    void setSellerFollow(String userId, Long sellerId, boolean active);
}

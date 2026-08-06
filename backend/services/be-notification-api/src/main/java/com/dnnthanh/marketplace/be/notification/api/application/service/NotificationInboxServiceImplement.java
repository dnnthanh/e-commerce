package com.dnnthanh.marketplace.be.notification.api.application.service;

import com.dnnthanh.marketplace.be.notification.api.application.port.in.NotificationInboxUseCase;
import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationInboxReadPort;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationInboxItem;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationPreferenceSnapshot;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationSearchCriteria;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Application boundary for the current user's durable notification inbox operations. */
@UseCase
@RequiredArgsConstructor
public class NotificationInboxServiceImplement implements NotificationInboxUseCase {
    private final NotificationInboxReadPort inboxReadPort;
    private final UserContext userContext;

    @Override
    public List<NotificationInboxItem> list(NotificationSearchCriteria criteria) {
        return inboxReadPort.list(currentUserId(), criteria);
    }

    @Override
    public long unreadCount() {
        return inboxReadPort.unreadCount(currentUserId());
    }

    @Override
    public void markRead(String notificationId) {
        inboxReadPort.markRead(currentUserId(), notificationId);
    }

    @Override
    public void markAllRead() {
        inboxReadPort.markAllRead(currentUserId());
    }

    @Override
    public NotificationPreferenceSnapshot preference() {
        return inboxReadPort.preferenceSnapshot(currentUserId());
    }

    @Override
    public NotificationPreferenceSnapshot savePreference(
            NotificationPreferenceSnapshot preference) {
        return inboxReadPort.savePreference(currentUserId(), preference);
    }

    @Override
    public void setSellerFollow(Long sellerId, boolean active) {
        inboxReadPort.setSellerFollow(currentUserId(), sellerId, active);
    }

    private String currentUserId() {
        return userContext.userId();
    }
}

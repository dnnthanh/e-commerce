package com.dnnthanh.marketplace.be.notification.api.adapter.in.web;

import com.dnnthanh.marketplace.be.notification.api.adapter.in.web.mapper.NotificationApiMapper;
import com.dnnthanh.marketplace.be.notification.api.api.NotificationApi;
import com.dnnthanh.marketplace.be.notification.api.api.request.search.NotificationSearchRequest;
import com.dnnthanh.marketplace.be.notification.api.api.response.NotificationView;
import com.dnnthanh.marketplace.be.notification.api.api.response.PreferenceView;
import com.dnnthanh.marketplace.be.notification.api.api.response.UnreadCount;
import com.dnnthanh.marketplace.be.notification.api.application.port.in.NotificationInboxUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationInboxUseCase inboxUseCase;
    private final NotificationApiMapper mapper;

    @Override
    public List<NotificationView> list(NotificationSearchRequest request) {
        return inboxUseCase.list(mapper.toCriteria(request)).stream().map(mapper::toView).toList();
    }

    @Override
    public UnreadCount unreadCount() {
        return new UnreadCount(inboxUseCase.unreadCount());
    }

    @Override
    public void markRead(String id) {
        inboxUseCase.markRead(id);
    }

    @Override
    public void markAllRead() {
        inboxUseCase.markAllRead();
    }

    @Override
    public PreferenceView preference() {
        return mapper.toView(inboxUseCase.preference());
    }

    @Override
    public PreferenceView preference(PreferenceView request) {
        return mapper.toView(inboxUseCase.savePreference(mapper.toSnapshot(request)));
    }

    @Override
    public void followSeller(Long sellerId) {
        inboxUseCase.setSellerFollow(sellerId, true);
    }

    @Override
    public void unfollowSeller(Long sellerId) {
        inboxUseCase.setSellerFollow(sellerId, false);
    }
}

package com.dnnthanh.marketplace.be.notification.api.api;

import com.dnnthanh.marketplace.be.notification.api.api.request.search.NotificationSearchRequest;
import com.dnnthanh.marketplace.be.notification.api.api.response.NotificationView;
import com.dnnthanh.marketplace.be.notification.api.api.response.PreferenceView;
import com.dnnthanh.marketplace.be.notification.api.api.response.UnreadCount;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/private/notifications")
public interface NotificationApi {

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    List<NotificationView> list(@Valid @ModelAttribute NotificationSearchRequest request);

    @GetMapping("/unread-count")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    UnreadCount unreadCount();

    @PostMapping("/{id}/read")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    void markRead(@PathVariable String id);

    @PostMapping("/read-all")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    void markAllRead();

    @GetMapping("/preferences")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    PreferenceView preference();

    @PutMapping("/preferences")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    PreferenceView preference(@RequestBody PreferenceView request);

    @PutMapping("/subscriptions/sellers/{sellerId}")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    void followSeller(@PathVariable Long sellerId);

    @DeleteMapping("/subscriptions/sellers/{sellerId}")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    void unfollowSeller(@PathVariable Long sellerId);
}

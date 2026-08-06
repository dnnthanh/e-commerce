package com.dnnthanh.marketplace.be.notification.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.notification.api.api.request.search.NotificationSearchRequest;
import com.dnnthanh.marketplace.be.notification.api.api.response.NotificationView;
import com.dnnthanh.marketplace.be.notification.api.api.response.PreferenceView;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationInboxItem;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationPreferenceSnapshot;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationSearchCriteria;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

/** Maps durable notification read models and preference transport contracts. */
@Mapper(config = PlatformMapperConfig.class)
public interface NotificationApiMapper extends MapperContract {
    NotificationSearchCriteria toCriteria(NotificationSearchRequest request);

    NotificationView toView(NotificationInboxItem item);

    PreferenceView toView(NotificationPreferenceSnapshot preference);

    NotificationPreferenceSnapshot toSnapshot(PreferenceView preference);
}

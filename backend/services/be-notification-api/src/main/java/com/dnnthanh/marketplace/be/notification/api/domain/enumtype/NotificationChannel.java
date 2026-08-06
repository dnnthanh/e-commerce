package com.dnnthanh.marketplace.be.notification.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Delivery channels. */
public enum NotificationChannel implements CodeEnum {
    IN_APP,
    EMAIL,
    PUSH,
    SMS
}

package com.dnnthanh.marketplace.be.notification.api.api.request.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped cursor request for the durable notification inbox. */
@Getter
@Setter
@NoArgsConstructor
public class NotificationSearchRequest {
    private LocalDateTime after;
    private String afterId;

    @Min(1)
    @Max(100)
    private int size = 50;
}

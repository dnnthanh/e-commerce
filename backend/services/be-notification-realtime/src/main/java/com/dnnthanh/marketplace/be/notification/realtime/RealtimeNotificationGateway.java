package com.dnnthanh.marketplace.be.notification.realtime;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Authenticated realtime SSE gateway; durable catch-up remains the Notification API responsibility.
 */
@RestController
@Slf4j
public class RealtimeNotificationGateway extends BaseDomainEventConsumer {

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    private final UserContext userContext;

    public RealtimeNotificationGateway(UserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * Opens an authenticated SSE stream; bearer tokens are never placed in the query string.
     *
     * @return long-lived SSE emitter
     */
    @GetMapping(value = "/private/notifications/stream", produces = "text/event-stream")
    @PreAuthorize("@authorizationService.hasPermission('NOTIFICATION_VIEW')")
    public SseEmitter stream() {
        String userId = userContext.userId();
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup =
                () -> emitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ignored -> cleanup.run());
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of("userId", userId)));
        } catch (IOException failure) {
            cleanup.run();
        }
        return emitter;
    }

    /**
     * Pushes only committed notification ids; missed frames are recovered by Notification API
     * catch-up.
     *
     * @param event typed realtime notification hint
     * @param userId Kafka partition key / notification recipient
     */
    @KafkaListener(
            topics = "marketplace.notification.realtime",
            groupId = "notification-realtime-v3")
    public void push(DomainEvent event, @Header(RECEIVED_KEY) String userId) {
        if (!accepts(event, "NOTIFICATION_READY")) {
            return;
        }
        for (SseEmitter emitter : emitters.getOrDefault(userId, new CopyOnWriteArrayList<>())) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .id(event.eventId())
                                .name("notification")
                                .data(payload(event)));
            } catch (IOException failure) {
                log.debug(
                        "notification_sse_connection_removed userId={} failure={}",
                        userId,
                        failure.toString());
                emitter.complete();
                emitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).remove(emitter);
            }
        }
    }
}

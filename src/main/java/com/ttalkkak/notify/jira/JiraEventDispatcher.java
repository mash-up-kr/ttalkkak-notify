package com.ttalkkak.notify.jira;

import com.ttalkkak.notify.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JiraEventDispatcher {

    private final List<JiraEventHandler> handlers;

    public Optional<NotificationEvent> dispatch(JiraWebhookPayload payload) {
        Optional<NotificationEvent> result = handlers.stream()
            .filter(h -> h.supports(payload))
            .findFirst()
            .map(h -> h.handle(payload));
        if (result.isEmpty()) {
            log.warn("Jira 웹훅 — 처리할 수 없는 이벤트 타입 (event: {})", payload.getWebhookEvent());
        }
        return result;
    }
}

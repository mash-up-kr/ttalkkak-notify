package com.ttalkkak.notify.jira;

import com.ttalkkak.notify.discord.model.DiscordMessage;
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

    public Optional<DiscordMessage> dispatch(JiraWebhookPayload payload) {
        log.debug("Dispatching Jira event: {}", payload.getWebhookEvent());
        return handlers.stream()
            .filter(h -> h.supports(payload))
            .findFirst()
            .map(h -> h.handle(payload));
    }
}

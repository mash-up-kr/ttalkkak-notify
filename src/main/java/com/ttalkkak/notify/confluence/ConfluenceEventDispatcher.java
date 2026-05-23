package com.ttalkkak.notify.confluence;

import com.ttalkkak.notify.discord.model.DiscordMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfluenceEventDispatcher {

    private final List<ConfluenceEventHandler> handlers;

    public Optional<DiscordMessage> dispatch(ConfluenceWebhookPayload payload) {
        log.debug("Dispatching Confluence event: {}", payload.getEvent());
        return handlers.stream()
            .filter(h -> h.supports(payload))
            .findFirst()
            .map(h -> h.handle(payload));
    }
}

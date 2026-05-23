package com.ttalkkak.notify.confluence;

import com.ttalkkak.notify.discord.model.DiscordMessage;

public interface ConfluenceEventHandler {
    boolean supports(ConfluenceWebhookPayload payload);
    DiscordMessage handle(ConfluenceWebhookPayload payload);
}

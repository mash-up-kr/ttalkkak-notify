package com.ttalkkak.notify.jira;

import com.ttalkkak.notify.discord.model.DiscordMessage;

public interface JiraEventHandler {
    boolean supports(JiraWebhookPayload payload);
    DiscordMessage handle(JiraWebhookPayload payload);
}

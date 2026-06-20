package com.ttalkkak.notify.jira;

import com.ttalkkak.notify.notification.NotificationEvent;

public interface JiraEventHandler {
    boolean supports(JiraWebhookPayload payload);
    NotificationEvent handle(JiraWebhookPayload payload);
}

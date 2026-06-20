package com.ttalkkak.notify.confluence;

import com.ttalkkak.notify.notification.NotificationEvent;

public interface ConfluenceEventHandler {
    boolean supports(ConfluenceWebhookPayload payload);
    NotificationEvent handle(ConfluenceWebhookPayload payload);
}

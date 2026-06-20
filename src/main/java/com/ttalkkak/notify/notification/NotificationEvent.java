package com.ttalkkak.notify.notification;

import java.util.List;

public record NotificationEvent(
    EventType eventType,
    String title,
    String description,
    String url,
    List<NotificationField> fields,
    Mention mention
) {}

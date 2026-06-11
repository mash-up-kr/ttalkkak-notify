package com.ttalkkak.notify.webhook;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebhookDeduplicator {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final Map<String, Instant> seenEvents = new ConcurrentHashMap<>();

    public boolean isDuplicate(String eventKey) {
        Instant now = Instant.now();
        seenEvents.values().removeIf(seenAt -> Duration.between(seenAt, now).compareTo(TTL) > 0);

        return seenEvents.putIfAbsent(eventKey, now) != null;
    }
}

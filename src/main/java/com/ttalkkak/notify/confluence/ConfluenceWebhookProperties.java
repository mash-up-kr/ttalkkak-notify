package com.ttalkkak.notify.confluence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("webhook.confluence")
public record ConfluenceWebhookProperties(String secret) {}

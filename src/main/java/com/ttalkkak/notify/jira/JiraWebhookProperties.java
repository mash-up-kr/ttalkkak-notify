package com.ttalkkak.notify.jira;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("webhook.jira")
public record JiraWebhookProperties(String secret) {}

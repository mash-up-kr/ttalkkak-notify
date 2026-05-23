package com.ttalkkak.notify.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("discord.webhook")
public record DiscordWebhookProperties(String task, String docs) {}

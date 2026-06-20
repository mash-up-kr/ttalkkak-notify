package com.ttalkkak.notify.chat;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("chat.broker")
public record ChatBrokerProperties(String url, String token, String target) {}

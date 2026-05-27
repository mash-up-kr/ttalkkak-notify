package com.ttalkkak.notify.confluence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("confluence.api")
public record ConfluenceApiProperties(String email, String token) {}

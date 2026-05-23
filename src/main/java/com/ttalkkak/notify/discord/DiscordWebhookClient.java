package com.ttalkkak.notify.discord;

import com.ttalkkak.notify.discord.model.DiscordMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class DiscordWebhookClient {

    private final DiscordWebhookProperties properties;
    private final RestClient restClient;

    public DiscordWebhookClient(DiscordWebhookProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public void sendToTask(DiscordMessage message) {
        send(properties.task(), withUsername(message, "작업 알림이"));
    }

    public void sendToDocs(DiscordMessage message) {
        send(properties.docs(), withUsername(message, "문서 알림이"));
    }

    private DiscordMessage withUsername(DiscordMessage message, String username) {
        return DiscordMessage.builder()
            .username(username)
            .content(message.getContent())
            .embeds(message.getEmbeds())
            .build();
    }

    private void send(String url, DiscordMessage message) {
        try {
            restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(message)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send Discord message to {}", url, e);
        }
    }
}

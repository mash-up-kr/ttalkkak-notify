package com.ttalkkak.notify.discord;

import com.ttalkkak.notify.discord.model.DiscordMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
@Slf4j
public class DiscordWebhookClient {

    private final DiscordWebhookProperties properties;
    private final RestClient restClient;

    public DiscordWebhookClient(DiscordWebhookProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
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
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(message)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (Exception e) {
                if (attempt < 2) {
                    log.warn("Discord 전송 실패 — 재시도합니다");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("Discord 전송 실패 — 재시도 1회, 최종 실패", e);
                }
            }
        }
    }
}

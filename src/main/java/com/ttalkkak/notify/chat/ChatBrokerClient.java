package com.ttalkkak.notify.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatBrokerClient {

    private final ChatBrokerProperties properties;
    private final RestClient restClient;

    public void send(String message) {
        ChatSendRequest request = new ChatSendRequest(message, properties.target());

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                restClient.post()
                        .uri(properties.url())
                        .header("Authorization", "Bearer " + properties.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode().is4xxClientError()) {
                    log.error("채팅 전송 실패 [status={}]", e.getStatusCode().value());
                    return;
                }
                if (attempt < 2) {
                    log.warn("채팅 전송 실패 [status={}] — 재시도합니다", e.getStatusCode().value());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("채팅 전송 실패 [status={}] — 재시도 1회, 최종 실패", e.getStatusCode().value());
                }
            } catch (Exception e) {
                if (attempt < 2) {
                    log.warn("채팅 전송 실패 (알 수 없는 오류) — 재시도합니다");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("채팅 전송 실패 (알 수 없는 오류) — 재시도 1회, 최종 실패", e);
                }
            }
        }
    }
}

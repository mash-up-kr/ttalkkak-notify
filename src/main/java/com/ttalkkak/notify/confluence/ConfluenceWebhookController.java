package com.ttalkkak.notify.confluence;

import com.ttalkkak.notify.discord.DiscordWebhookClient;
import com.ttalkkak.notify.webhook.WebhookDeduplicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConfluenceWebhookController {

    private final ConfluenceWebhookProperties properties;
    private final ConfluenceEventDispatcher dispatcher;
    private final DiscordWebhookClient discordWebhookClient;
    private final WebhookDeduplicator deduplicator;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook/confluence")
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestParam(value = "token", required = false) String token,
            @RequestHeader(value = "X-Real-IP", required = false) String remoteIp) throws Exception {

        // Confluence Cloud는 X-Hub-Signature 미지원 → URL query token으로 대체
        if (!properties.secret().equals(token)) {
            log.warn("Confluence 웹훅 인증 실패 [ip={}]", remoteIp);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        ConfluenceWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, ConfluenceWebhookPayload.class);
        } catch (Exception e) {
            log.error("Confluence 웹훅 페이로드 파싱 실패 [ip={}]: {}", remoteIp, e.getMessage());
            throw e;
        }

        String eventKey = payload.getEventKey();
        if (eventKey != null && deduplicator.isDuplicate(eventKey)) {
            log.info("Confluence 웹훅 — 중복 이벤트 스킵 [key={}]", eventKey);
            return ResponseEntity.ok().build();
        }

        dispatcher.dispatch(payload)
            .ifPresent(discordWebhookClient::sendToDocs);

        return ResponseEntity.ok().build();
    }
}

package com.ttalkkak.notify.jira;

import com.ttalkkak.notify.discord.DiscordWebhookClient;
import com.ttalkkak.notify.security.HmacSignatureVerifier;
import com.ttalkkak.notify.webhook.WebhookDeduplicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@Slf4j
public class JiraWebhookController {

    private final JiraWebhookProperties properties;
    private final HmacSignatureVerifier signatureVerifier;
    private final JiraEventDispatcher dispatcher;
    private final DiscordWebhookClient discordWebhookClient;
    private final WebhookDeduplicator deduplicator;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook/jira")
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature", required = false) String signature,
            @RequestHeader(value = "X-Real-IP", required = false) String remoteIp) throws Exception {

        if (!signatureVerifier.verify(rawBody, properties.secret(), signature)) {
            log.warn("Jira 웹훅 인증 실패 [ip={}]", remoteIp);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        JiraWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, JiraWebhookPayload.class);
        } catch (Exception e) {
            log.error("Jira 웹훅 페이로드 파싱 실패 [ip={}]: {}", remoteIp, e.getMessage());
            throw e;
        }

        String eventKey = payload.getEventKey();
        if (eventKey != null && deduplicator.isDuplicate(eventKey)) {
            log.info("Jira 웹훅 — 중복 이벤트 스킵 [key={}]", eventKey);
            return ResponseEntity.ok().build();
        }

        dispatcher.dispatch(payload)
            .ifPresent(discordWebhookClient::sendToTask);

        return ResponseEntity.ok().build();
    }
}

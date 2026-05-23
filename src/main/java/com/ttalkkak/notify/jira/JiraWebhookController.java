package com.ttalkkak.notify.jira;

import com.ttalkkak.notify.discord.DiscordWebhookClient;
import com.ttalkkak.notify.security.HmacSignatureVerifier;
import lombok.RequiredArgsConstructor;
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
public class JiraWebhookController {

    private final JiraWebhookProperties properties;
    private final HmacSignatureVerifier signatureVerifier;
    private final JiraEventDispatcher dispatcher;
    private final DiscordWebhookClient discordWebhookClient;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook/jira")
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature", required = false) String signature) throws Exception {

        if (!signatureVerifier.verify(rawBody, properties.secret(), signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        JiraWebhookPayload payload = objectMapper.readValue(rawBody, JiraWebhookPayload.class);
        dispatcher.dispatch(payload)
            .ifPresent(discordWebhookClient::sendToTask);

        return ResponseEntity.ok().build();
    }
}

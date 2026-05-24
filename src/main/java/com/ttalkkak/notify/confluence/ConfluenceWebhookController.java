package com.ttalkkak.notify.confluence;

import com.ttalkkak.notify.discord.DiscordWebhookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
public class ConfluenceWebhookController {

    private final ConfluenceWebhookProperties properties;
    private final ConfluenceEventDispatcher dispatcher;
    private final DiscordWebhookClient discordWebhookClient;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook/confluence")
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestParam(value = "token", required = false) String token) throws Exception {

        // Confluence Cloud does not support webhook secrets (X-Hub-Signature), so token-in-URL is used instead
        if (!properties.secret().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        ConfluenceWebhookPayload payload = objectMapper.readValue(rawBody, ConfluenceWebhookPayload.class);
        dispatcher.dispatch(payload)
            .ifPresent(discordWebhookClient::sendToDocs);

        return ResponseEntity.ok().build();
    }
}

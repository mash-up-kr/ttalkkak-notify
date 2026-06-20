package com.ttalkkak.notify.jira;

import com.ttalkkak.notify.chat.ChatBrokerClient;
import com.ttalkkak.notify.chat.ChatMessageFormatter;
import com.ttalkkak.notify.discord.DiscordMessageFormatter;
import com.ttalkkak.notify.discord.DiscordWebhookClient;
import com.ttalkkak.notify.notification.EventType;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.security.HmacSignatureVerifier;
import com.ttalkkak.notify.webhook.WebhookDeduplicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JiraWebhookController.class)
@Import({HmacSignatureVerifier.class, WebhookDeduplicator.class})
class JiraWebhookControllerTest {

    private static final String SECRET = "test-secret";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JiraWebhookProperties properties;

    @MockitoBean
    private JiraEventDispatcher dispatcher;

    @MockitoBean
    private DiscordWebhookClient discordWebhookClient;

    @MockitoBean
    private DiscordMessageFormatter discordMessageFormatter;

    @MockitoBean
    private ChatBrokerClient chatBrokerClient;

    @MockitoBean
    private ChatMessageFormatter chatMessageFormatter;

    @BeforeEach
    void setUp() {
        given(properties.secret()).willReturn(SECRET);
        given(dispatcher.dispatch(any())).willReturn(Optional.of(
            new NotificationEvent(EventType.JIRA_ISSUE_CREATED, "제목", null, null, null, null)
        ));
    }

    @Test
    void duplicateEvent_onlyFirstSendsToDiscord() throws Exception {
        String body = """
                {"webhookEvent":"jira:issue_created","timestamp":1700000000001,"issue":{"key":"DDK-101","fields":{"summary":"Test issue"}}}
                """;

        sendWebhook(body).andExpect(status().isOk());
        sendWebhook(body).andExpect(status().isOk());

        verify(discordWebhookClient, times(1)).sendToTask(any());
    }

    @Test
    void differentEvents_eachSentSeparately() throws Exception {
        String body1 = """
                {"webhookEvent":"jira:issue_created","timestamp":1700000000002,"issue":{"key":"DDK-102","fields":{"summary":"Test issue 1"}}}
                """;
        String body2 = """
                {"webhookEvent":"jira:issue_created","timestamp":1700000000003,"issue":{"key":"DDK-103","fields":{"summary":"Test issue 2"}}}
                """;

        sendWebhook(body1).andExpect(status().isOk());
        sendWebhook(body2).andExpect(status().isOk());

        verify(discordWebhookClient, times(2)).sendToTask(any());
    }

    @Test
    void noTimestamp_dedupNotApplied_stillSentEachTime() throws Exception {
        String body = """
                {"webhookEvent":"jira:issue_created","issue":{"key":"DDK-104","fields":{"summary":"Test issue without timestamp"}}}
                """;

        sendWebhook(body).andExpect(status().isOk());
        sendWebhook(body).andExpect(status().isOk());

        verify(discordWebhookClient, times(2)).sendToTask(any());
    }

    private ResultActions sendWebhook(String body) throws Exception {
        return mockMvc.perform(post("/webhook/jira")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Hub-Signature", "sha256=" + hmacSha256(body, SECRET))
                .content(body));
    }

    private static String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

package com.ttalkkak.notify.confluence;

import com.ttalkkak.notify.discord.DiscordWebhookClient;
import com.ttalkkak.notify.discord.model.DiscordMessage;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfluenceWebhookController.class)
@Import(WebhookDeduplicator.class)
class ConfluenceWebhookControllerTest {

    private static final String TOKEN = "test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfluenceWebhookProperties properties;

    @MockitoBean
    private ConfluenceEventDispatcher dispatcher;

    @MockitoBean
    private DiscordWebhookClient discordWebhookClient;

    @BeforeEach
    void setUp() {
        given(properties.secret()).willReturn(TOKEN);
        given(dispatcher.dispatch(any())).willReturn(Optional.of(DiscordMessage.builder().build()));
    }

    @Test
    void duplicateEvent_onlyFirstSendsToDiscord() throws Exception {
        String body = """
                {"userAccountId":"acc-1","timestamp":1700000000001,"page":{"id":"2001","title":"Test Page","spaceKey":"DDK","self":"https://example.com/2001"}}
                """;

        sendWebhook(body).andExpect(status().isOk());
        sendWebhook(body).andExpect(status().isOk());

        verify(discordWebhookClient, times(1)).sendToDocs(any());
    }

    @Test
    void differentEvents_eachSentSeparately() throws Exception {
        String body1 = """
                {"userAccountId":"acc-1","timestamp":1700000000002,"page":{"id":"2002","title":"Test Page A","spaceKey":"DDK","self":"https://example.com/2002"}}
                """;
        String body2 = """
                {"userAccountId":"acc-1","timestamp":1700000000003,"page":{"id":"2003","title":"Test Page B","spaceKey":"DDK","self":"https://example.com/2003"}}
                """;

        sendWebhook(body1).andExpect(status().isOk());
        sendWebhook(body2).andExpect(status().isOk());

        verify(discordWebhookClient, times(2)).sendToDocs(any());
    }

    @Test
    void noTimestamp_dedupNotApplied_stillSentEachTime() throws Exception {
        String body = """
                {"userAccountId":"acc-1","page":{"id":"2004","title":"Test Page C","spaceKey":"DDK","self":"https://example.com/2004"}}
                """;

        sendWebhook(body).andExpect(status().isOk());
        sendWebhook(body).andExpect(status().isOk());

        verify(discordWebhookClient, times(2)).sendToDocs(any());
    }

    private ResultActions sendWebhook(String body) throws Exception {
        return mockMvc.perform(post("/webhook/confluence")
                .param("token", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}

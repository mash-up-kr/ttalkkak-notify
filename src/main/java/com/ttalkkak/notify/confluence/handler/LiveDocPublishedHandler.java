package com.ttalkkak.notify.confluence.handler;

import com.ttalkkak.notify.confluence.ConfluenceEventHandler;
import com.ttalkkak.notify.confluence.ConfluenceWebhookPayload;
import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordField;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.user.UserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LiveDocPublishedHandler implements ConfluenceEventHandler {

    private final UserMappingRepository userMappingRepository;

    @Override
    public boolean supports(ConfluenceWebhookPayload payload) {
        return "live_doc_published".equals(payload.getEvent());
    }

    @Override
    public DiscordMessage handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Page page = payload.getPage();

        List<DiscordField> fields = new ArrayList<>();
        userMappingRepository.findName(payload.getUserAccountId()).ifPresent(name ->
            fields.add(DiscordField.builder()
                .name("발행자").value(name).inline(true).build())
        );

        DiscordEmbed embed = DiscordEmbed.builder()
            .title("📘 라이브 문서 발행: " + page.getTitle())
            .color(EmbedColor.CONFLUENCE_LIVE_DOC_PUBLISHED)
            .url(page.getSelf())
            .fields(fields.isEmpty() ? null : fields)
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }
}

package com.ttalkkak.notify.confluence.handler;

import com.ttalkkak.notify.confluence.ConfluenceEventHandler;
import com.ttalkkak.notify.confluence.ConfluenceWebhookPayload;
import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordField;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PageUpdatedHandler implements ConfluenceEventHandler {

    @Override
    public boolean supports(ConfluenceWebhookPayload payload) {
        return "page_updated".equals(payload.getEvent());
    }

    @Override
    public DiscordMessage handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Page page = payload.getPage();

        List<DiscordField> fields = new ArrayList<>();
        if (page.getSpace() != null) {
            fields.add(DiscordField.builder()
                .name("스페이스").value(page.getSpace().getName()).inline(true).build());
        }
        if (page.getVersion() != null && page.getVersion().getBy() != null) {
            fields.add(DiscordField.builder()
                .name("수정자").value(page.getVersion().getBy().getDisplayName()).inline(true).build());
        }

        DiscordEmbed embed = DiscordEmbed.builder()
            .title("✏️ 문서 수정: " + page.getTitle())
            .color(EmbedColor.CONFLUENCE_PAGE_UPDATED)
            .url(page.getWebUrl())
            .fields(fields.isEmpty() ? null : fields)
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }
}

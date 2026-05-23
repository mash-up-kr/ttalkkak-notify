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

@Component("confluenceCommentCreatedHandler")
public class CommentCreatedHandler implements ConfluenceEventHandler {

    @Override
    public boolean supports(ConfluenceWebhookPayload payload) {
        return "comment_created".equals(payload.getEvent());
    }

    @Override
    public DiscordMessage handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Comment comment = payload.getComment();
        ConfluenceWebhookPayload.Page page = comment != null ? comment.getPage() : null;

        String pageTitle = page != null ? page.getTitle() : "알 수 없음";
        String pageUrl = page != null ? page.getWebUrl() : null;

        List<DiscordField> fields = new ArrayList<>();
        if (page != null && page.getSpace() != null) {
            fields.add(DiscordField.builder()
                .name("스페이스").value(page.getSpace().getName()).inline(true).build());
        }
        if (comment != null && comment.getAuthor() != null) {
            fields.add(DiscordField.builder()
                .name("작성자").value(comment.getAuthor().getDisplayName()).inline(true).build());
        }

        DiscordEmbed embed = DiscordEmbed.builder()
            .title("💬 댓글: " + pageTitle)
            .color(EmbedColor.CONFLUENCE_COMMENT)
            .url(pageUrl)
            .fields(fields.isEmpty() ? null : fields)
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }
}

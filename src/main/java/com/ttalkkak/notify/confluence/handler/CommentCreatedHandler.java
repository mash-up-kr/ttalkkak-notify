package com.ttalkkak.notify.confluence.handler;

import com.ttalkkak.notify.confluence.ConfluenceApiClient;
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
public class CommentCreatedHandler implements ConfluenceEventHandler {

    private final UserMappingRepository userMappingRepository;
    private final ConfluenceApiClient confluenceApiClient;

    @Override
    public boolean supports(ConfluenceWebhookPayload payload) {
        return "comment_created".equals(payload.getEvent());
    }

    @Override
    public DiscordMessage handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Comment comment = payload.getComment();
        ConfluenceWebhookPayload.Page page = comment != null ? comment.getParent() : null;

        String pageTitle = page != null ? page.getTitle() : "알 수 없음";
        String commentUrl = comment != null ? comment.getSelf() : null;

        String bodyText = comment != null ? confluenceApiClient.fetchCommentBody(comment.getId()) : null;

        List<DiscordField> fields = new ArrayList<>();
        userMappingRepository.findName(payload.getUserAccountId()).ifPresent(name ->
            fields.add(DiscordField.builder()
                .name("작성자").value(name).inline(true).build())
        );

        DiscordEmbed embed = DiscordEmbed.builder()
            .title("💬 댓글: " + pageTitle)
            .description(bodyText)
            .color(EmbedColor.CONFLUENCE_COMMENT)
            .url(commentUrl)
            .fields(fields.isEmpty() ? null : fields)
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }
}

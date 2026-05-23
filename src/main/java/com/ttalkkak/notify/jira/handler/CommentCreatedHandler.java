package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordMessage;

import java.util.List;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import org.springframework.stereotype.Component;


@Component("jiraCommentCreatedHandler")
public class CommentCreatedHandler implements JiraEventHandler {

    @Override
    public boolean supports(JiraWebhookPayload payload) {
        return "comment_created".equals(payload.getWebhookEvent());
    }

    @Override
    public DiscordMessage handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Comment comment = payload.getComment();

        String title = "💬 [" + issue.getKey() + "] " + issue.getFields().getSummary();

        String body = comment != null && comment.getBody() != null ? comment.getBody() : "";
        String truncatedBody = body.length() > 500 ? body.substring(0, 500) + "..." : body;

        String authorName = comment != null && comment.getAuthor() != null
            ? comment.getAuthor().getDisplayName() : "알 수 없음";
        String description = truncatedBody.isEmpty() ? null : authorName + ": " + truncatedBody;

        DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .description(description)
            .color(EmbedColor.COMMENT)
            .url(issue.getWebUrl())
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }
}

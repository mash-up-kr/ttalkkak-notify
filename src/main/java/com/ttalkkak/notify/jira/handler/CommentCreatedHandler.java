package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordMessage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import com.ttalkkak.notify.user.UserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("jiraCommentCreatedHandler")
@RequiredArgsConstructor
public class CommentCreatedHandler implements JiraEventHandler {

    private static final Pattern MENTION_PATTERN = Pattern.compile("\\[~accountid:([^\\]]+)\\]");

    private final UserMappingRepository userMappingRepository;

    private String resolveMentions(String body) {
        Matcher matcher = MENTION_PATTERN.matcher(body);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String accountId = matcher.group(1);
            String replacement = userMappingRepository.findDiscordId(accountId)
                .map(id -> "<@" + id + ">")
                .orElseGet(() -> userMappingRepository.findName(accountId).orElse("@" + accountId));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public boolean supports(JiraWebhookPayload payload) {
        return "comment_created".equals(payload.getWebhookEvent());
    }

    @Override
    public DiscordMessage handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Comment comment = payload.getComment();

        String title = "💬 [" + issue.getKey() + "] " + issue.getFields().getSummary();

        String body = comment != null && comment.getBody() != null
            ? resolveMentions(comment.getBody()) : "";
        String truncatedBody = body.length() > 500 ? body.substring(0, 500) + "..." : body;

        String authorName = comment != null && comment.getAuthor() != null
            ? userMappingRepository.findName(comment.getAuthor().getAccountId()).orElse(comment.getAuthor().getDisplayName())
            : "알 수 없음";
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

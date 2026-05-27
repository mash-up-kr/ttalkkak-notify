package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import com.ttalkkak.notify.user.UserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("jiraCommentCreatedHandler")
@RequiredArgsConstructor
public class CommentCreatedHandler implements JiraEventHandler {

    private static final Pattern MENTION_PATTERN = Pattern.compile("\\[~accountid:([^\\]]+)\\]");
    private static final int MAX_BODY_LENGTH = 100;

    private final UserMappingRepository userMappingRepository;

    @Override
    public boolean supports(JiraWebhookPayload payload) {
        return "comment_created".equals(payload.getWebhookEvent());
    }

    @Override
    public DiscordMessage handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Comment comment = payload.getComment();

        String title = "💬 [" + issue.getKey() + "] " + issue.getFields().getSummary();

        String rawBody = comment != null && comment.getBody() != null ? comment.getBody() : null;

        String pingContent = rawBody != null ? extractMentionPings(rawBody) : null;
        String processedBody = rawBody != null ? stripAndTruncate(rawBody) : null;

        String authorName = comment != null && comment.getAuthor() != null
            ? userMappingRepository.findName(comment.getAuthor().getAccountId()).orElse(comment.getAuthor().getDisplayName())
            : "알 수 없음";

        String description = (processedBody != null && !processedBody.isEmpty())
            ? authorName + ": " + processedBody : null;

        String content = pingContent != null ? pingContent + " 확인 부탁드려요!" : null;

        DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .description(description)
            .color(EmbedColor.COMMENT)
            .url(issue.getWebUrl())
            .build();

        return DiscordMessage.builder().content(content).embeds(List.of(embed)).build();
    }

    private String extractMentionPings(String body) {
        Matcher matcher = MENTION_PATTERN.matcher(body);
        List<String> pings = new ArrayList<>();
        while (matcher.find()) {
            String accountId = matcher.group(1);
            userMappingRepository.findDiscordId(accountId)
                .map(id -> "<@" + id + ">")
                .ifPresent(pings::add);
        }
        return pings.isEmpty() ? null : String.join(", ", pings);
    }

    private String stripAndTruncate(String body) {
        Matcher matcher = MENTION_PATTERN.matcher(body);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String accountId = matcher.group(1);
            String name = userMappingRepository.findName(accountId).orElse("@" + accountId);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(name));
        }
        matcher.appendTail(sb);

        String text = sb.toString().trim();
        return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "..." : text;
    }
}

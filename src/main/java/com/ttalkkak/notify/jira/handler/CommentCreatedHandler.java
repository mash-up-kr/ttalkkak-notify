package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.discord.CommentBodySanitizer;
import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import com.ttalkkak.notify.notification.EventType;
import com.ttalkkak.notify.notification.Mention;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.user.UserInfo;
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
    public NotificationEvent handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Comment comment = payload.getComment();

        String rawBody = comment != null && comment.getBody() != null ? comment.getBody() : null;
        String authorName = comment != null && comment.getAuthor() != null
            ? userMappingRepository.findName(comment.getAuthor().getAccountId()).orElse(comment.getAuthor().getDisplayName())
            : "알 수 없음";

        return new NotificationEvent(
            EventType.JIRA_COMMENT_CREATED,
            "💬 [" + issue.getKey() + "] " + issue.getFields().getSummary(),
            buildDescription(rawBody, authorName),
            issue.getWebUrl(),
            null,
            buildMention(rawBody, authorName)
        );
    }

    private String buildDescription(String rawBody, String authorName) {
        if (rawBody == null) return null;
        String text = stripAndTruncate(rawBody);
        return (text != null && !text.isEmpty()) ? authorName + ": " + text : null;
    }

    private Mention buildMention(String rawBody, String authorName) {
        if (rawBody == null) return null;
        List<UserInfo> targets = extractMentionedUsers(rawBody);
        if (targets.isEmpty()) return null;
        return new Mention(authorName + "님이 댓글에서 ", targets, " 님을 언급했습니다.");
    }

    private List<UserInfo> extractMentionedUsers(String body) {
        Matcher matcher = MENTION_PATTERN.matcher(body);
        List<UserInfo> users = new ArrayList<>();
        while (matcher.find()) {
            String accountId = matcher.group(1);
            users.add(userMappingRepository.findUser(accountId)
                .orElseGet(() -> new UserInfo(accountId, accountId, null)));
        }
        return users;
    }

    private String stripAndTruncate(String body) {
        Matcher matcher = MENTION_PATTERN.matcher(body);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String name = "@" + userMappingRepository.findName(matcher.group(1)).orElse(matcher.group(1));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(name));
        }
        matcher.appendTail(sb);
        String text = CommentBodySanitizer.fromJiraWikiMarkup(sb.toString().trim());
        return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "..." : text;
    }
}

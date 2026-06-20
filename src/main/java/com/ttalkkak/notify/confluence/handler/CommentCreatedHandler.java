package com.ttalkkak.notify.confluence.handler;

import com.ttalkkak.notify.confluence.ConfluenceApiClient;
import com.ttalkkak.notify.confluence.ConfluenceEventHandler;
import com.ttalkkak.notify.confluence.ConfluenceWebhookPayload;
import com.ttalkkak.notify.notification.CommentBodySanitizer;
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

@Component
@RequiredArgsConstructor
public class CommentCreatedHandler implements ConfluenceEventHandler {

    private static final Pattern MENTION_PATTERN = Pattern.compile("<ri:user ri:account-id=\"([^\"]+)\"[^/]*/>");
    private static final int MAX_BODY_LENGTH = 100;

    private final UserMappingRepository userMappingRepository;
    private final ConfluenceApiClient confluenceApiClient;

    @Override
    public boolean supports(ConfluenceWebhookPayload payload) {
        return "comment_created".equals(payload.getEvent());
    }

    @Override
    public NotificationEvent handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Comment comment = payload.getComment();
        ConfluenceWebhookPayload.Page page = comment != null ? comment.getParent() : null;

        String pageTitle = page != null ? page.getTitle() : "알 수 없음";
        String commentUrl = comment != null ? comment.getSelf() : null;
        String authorName = userMappingRepository.findName(payload.getUserAccountId()).orElse(null);
        String rawHtml = comment != null ? confluenceApiClient.fetchCommentBody(comment.getId()) : null;

        return new NotificationEvent(
            EventType.CONFLUENCE_COMMENT_CREATED,
            "💬 댓글: " + pageTitle,
            buildDescription(rawHtml, authorName),
            commentUrl,
            null,
            buildMention(rawHtml, authorName)
        );
    }

    private String buildDescription(String rawHtml, String authorName) {
        if (rawHtml == null) return null;
        String text = stripAndTruncate(rawHtml);
        if (text == null || text.isEmpty()) return null;
        return authorName != null ? authorName + ": " + text : text;
    }

    private Mention buildMention(String rawHtml, String authorName) {
        if (rawHtml == null) return null;
        List<UserInfo> targets = extractMentionedUsers(rawHtml);
        if (targets.isEmpty()) return null;
        String prefix = authorName != null ? authorName + "님이 댓글에서 " : "댓글에서 ";
        return new Mention(prefix, targets, " 님을 언급했습니다.");
    }

    private List<UserInfo> extractMentionedUsers(String storageHtml) {
        Matcher matcher = MENTION_PATTERN.matcher(storageHtml);
        List<UserInfo> users = new ArrayList<>();
        while (matcher.find()) {
            String accountId = matcher.group(1);
            users.add(userMappingRepository.findUser(accountId)
                .orElseGet(() -> new UserInfo(null, accountId, null)));
        }
        return users;
    }

    private String stripAndTruncate(String storageHtml) {
        Matcher matcher = MENTION_PATTERN.matcher(storageHtml);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String name = "@" + userMappingRepository.findName(matcher.group(1)).orElse(matcher.group(1));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(name));
        }
        matcher.appendTail(sb);
        String text = CommentBodySanitizer.fromConfluenceStorageHtml(sb.toString());
        return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "..." : text;
    }
}

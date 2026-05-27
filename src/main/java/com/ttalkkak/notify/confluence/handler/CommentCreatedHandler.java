package com.ttalkkak.notify.confluence.handler;

import com.ttalkkak.notify.confluence.ConfluenceApiClient;
import com.ttalkkak.notify.confluence.ConfluenceEventHandler;
import com.ttalkkak.notify.confluence.ConfluenceWebhookPayload;
import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
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
    public DiscordMessage handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Comment comment = payload.getComment();
        ConfluenceWebhookPayload.Page page = comment != null ? comment.getParent() : null;

        String pageTitle = page != null ? page.getTitle() : "알 수 없음";
        String commentUrl = comment != null ? comment.getSelf() : null;

        // Confluence 페이로드에 표시 이름이 없어 매핑 실패 시 null로 폴백
        String authorName = userMappingRepository.findName(payload.getUserAccountId()).orElse(null);

        String rawHtml = comment != null ? confluenceApiClient.fetchCommentBody(comment.getId()) : null;

        String content = null;
        if (rawHtml != null) {
            String pingPart = extractMentionPings(rawHtml);
            if (pingPart != null) {
                String authorPart = authorName != null ? authorName + "님이 댓글에서 " : "댓글에서 ";
                content = authorPart + pingPart + " 님을 언급했습니다.";
            }
        }

        String processedBody = rawHtml != null ? stripAndTruncate(rawHtml) : null;
        String description = null;
        if (processedBody != null && !processedBody.isEmpty()) {
            description = authorName != null ? authorName + ": " + processedBody : processedBody;
        }

        DiscordEmbed embed = DiscordEmbed.builder()
            .title("💬 댓글: " + pageTitle)
            .description(description)
            .color(EmbedColor.CONFLUENCE_COMMENT)
            .url(commentUrl)
            .build();

        return DiscordMessage.builder().content(content).embeds(List.of(embed)).build();
    }

    private String extractMentionPings(String storageHtml) {
        Matcher matcher = MENTION_PATTERN.matcher(storageHtml);
        List<String> pings = new ArrayList<>();
        while (matcher.find()) {
            String accountId = matcher.group(1);
            userMappingRepository.findDiscordId(accountId)
                .map(id -> "<@" + id + ">")
                .ifPresent(pings::add);
        }
        return pings.isEmpty() ? null : String.join(", ", pings);
    }

    private String stripAndTruncate(String storageHtml) {
        Matcher matcher = MENTION_PATTERN.matcher(storageHtml);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String accountId = matcher.group(1);
            String name = "@" + userMappingRepository.findName(accountId).orElse(accountId);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(name));
        }
        matcher.appendTail(sb);

        String text = sb.toString().replaceAll("<[^>]+>", "").trim();
        return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "..." : text;
    }
}

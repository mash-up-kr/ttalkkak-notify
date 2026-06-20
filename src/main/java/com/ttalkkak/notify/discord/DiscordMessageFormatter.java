package com.ttalkkak.notify.discord;

import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordField;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.notification.EventType;
import com.ttalkkak.notify.notification.Mention;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.notification.NotificationField;
import com.ttalkkak.notify.user.UserInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DiscordMessageFormatter {

    public DiscordMessage format(NotificationEvent event) {
        DiscordEmbed embed = DiscordEmbed.builder()
            .title(event.title())
            .description(event.description())
            .url(event.url())
            .color(colorFor(event.eventType()))
            .fields(toDiscordFields(event.fields()))
            .build();
        return DiscordMessage.builder()
            .content(buildContent(event.mention()))
            .embeds(List.of(embed))
            .build();
    }

    private String buildContent(Mention mention) {
        if (mention == null || mention.targets().isEmpty()) return null;
        String pings = mention.targets().stream()
            .map(UserInfo::getDiscordId)
            .filter(Objects::nonNull)
            .map(id -> "<@" + id + ">")
            .collect(Collectors.joining(", "));
        if (pings.isBlank()) return null;
        return mention.prefix() + pings + mention.suffix();
    }

    private int colorFor(EventType type) {
        return switch (type) {
            case JIRA_ISSUE_CREATED -> EmbedColor.ISSUE_CREATED;
            case JIRA_ASSIGNEE_CHANGED -> EmbedColor.ASSIGNEE_CHANGED;
            case JIRA_STATUS_CHANGED -> EmbedColor.STATUS_CHANGED;
            case JIRA_COMMENT_CREATED -> EmbedColor.COMMENT;
            case CONFLUENCE_PAGE_CREATED -> EmbedColor.CONFLUENCE_PAGE_CREATED;
            case CONFLUENCE_PAGE_UPDATED -> EmbedColor.CONFLUENCE_PAGE_UPDATED;
            case CONFLUENCE_LIVE_DOC_CREATED -> EmbedColor.CONFLUENCE_LIVE_DOC_CREATED;
            case CONFLUENCE_COMMENT_CREATED -> EmbedColor.CONFLUENCE_COMMENT;
        };
    }

    private List<DiscordField> toDiscordFields(List<NotificationField> fields) {
        if (fields == null || fields.isEmpty()) return null;
        return fields.stream()
            .map(f -> DiscordField.builder().name(f.name()).value(f.value()).inline(f.inline()).build())
            .toList();
    }
}

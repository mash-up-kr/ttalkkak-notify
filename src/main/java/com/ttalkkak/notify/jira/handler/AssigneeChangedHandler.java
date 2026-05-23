package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordField;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AssigneeChangedHandler implements JiraEventHandler {

    @Override
    public boolean supports(JiraWebhookPayload payload) {
        if (!"jira:issue_updated".equals(payload.getWebhookEvent())) return false;
        if (payload.getChangelog() == null || payload.getChangelog().getItems() == null) return false;
        return payload.getChangelog().getItems().stream()
            .anyMatch(item -> "assignee".equals(item.getField()));
    }

    @Override
    public DiscordMessage handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();

        JiraWebhookPayload.ChangelogItem assigneeChange = payload.getChangelog().getItems().stream()
            .filter(item -> "assignee".equals(item.getField()))
            .findFirst().orElseThrow();

        String title = "👤 [" + issue.getKey() + "] " + issue.getFields().getSummary();

        List<DiscordField> embedFields = new ArrayList<>();
        embedFields.add(DiscordField.builder()
            .name("이전 담당자")
            .value(assigneeChange.getFromString() != null ? assigneeChange.getFromString() : "없음")
            .inline(true).build());
        embedFields.add(DiscordField.builder()
            .name("새 담당자")
            .value(assigneeChange.getToValue() != null ? assigneeChange.getToValue() : "없음")
            .inline(true).build());

        DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .color(EmbedColor.ASSIGNEE_CHANGED)
            .url(issue.getWebUrl())
            .fields(embedFields)
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }
}

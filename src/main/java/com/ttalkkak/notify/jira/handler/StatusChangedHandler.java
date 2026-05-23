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
public class StatusChangedHandler implements JiraEventHandler {

    @Override
    public boolean supports(JiraWebhookPayload payload) {
        if (!"jira:issue_updated".equals(payload.getWebhookEvent())) return false;
        if (payload.getChangelog() == null || payload.getChangelog().getItems() == null) return false;
        return payload.getChangelog().getItems().stream()
            .anyMatch(item -> "status".equals(item.getField()));
    }

    @Override
    public DiscordMessage handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Fields fields = issue.getFields();

        JiraWebhookPayload.ChangelogItem statusChange = payload.getChangelog().getItems().stream()
            .filter(item -> "status".equals(item.getField()))
            .findFirst().orElseThrow();

        String title = "🔄 [" + issue.getKey() + "] " + fields.getSummary();
        String statusChangeValue = statusChange.getFromString() + " → " + statusChange.getToValue();

        List<DiscordField> embedFields = new ArrayList<>();
        embedFields.add(DiscordField.builder()
            .name("상태 변경").value(statusChangeValue).build());
        if (fields.getAssignee() != null) {
            embedFields.add(DiscordField.builder()
                .name("담당자").value(fields.getAssignee().getDisplayName()).inline(true).build());
        }
        if (fields.getPriority() != null) {
            embedFields.add(DiscordField.builder()
                .name("우선순위").value(fields.getPriority().getName()).inline(true).build());
        }

        DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .color(EmbedColor.STATUS_CHANGED)
            .url(issue.getWebUrl())
            .fields(embedFields)
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }
}

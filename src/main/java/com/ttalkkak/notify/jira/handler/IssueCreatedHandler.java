package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordField;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import com.ttalkkak.notify.user.UserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IssueCreatedHandler implements JiraEventHandler {

    private final UserMappingRepository userMappingRepository;

    @Override
    public boolean supports(JiraWebhookPayload payload) {
        return "jira:issue_created".equals(payload.getWebhookEvent());
    }

    @Override
    public DiscordMessage handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Fields fields = issue.getFields();

        String title = "🆕 [" + issue.getKey() + "] " + fields.getSummary();
        String description = buildAssigneeMention(fields.getAssignee());

        List<DiscordField> embedFields = new ArrayList<>();
        if (fields.getDecisionLevel() != null) {
            embedFields.add(DiscordField.builder()
                .name("Decision Level").value(fields.getDecisionLevel().getValue()).inline(true).build());
        }
        if (fields.getPriority() != null) {
            embedFields.add(DiscordField.builder()
                .name("우선순위").value(fields.getPriority().getName()).inline(true).build());
        }

        DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .description(description)
            .color(EmbedColor.ISSUE_CREATED)
            .url(issue.getWebUrl())
            .fields(embedFields.isEmpty() ? null : embedFields)
            .build();

        return DiscordMessage.builder().embeds(List.of(embed)).build();
    }

    private String buildAssigneeMention(JiraWebhookPayload.User assignee) {
        if (assignee == null) return null;
        return userMappingRepository.findDiscordId(assignee.getAccountId())
            .map(id -> "담당자: <@" + id + ">")
            .orElseGet(() -> "담당자: " + assignee.getDisplayName());
    }
}

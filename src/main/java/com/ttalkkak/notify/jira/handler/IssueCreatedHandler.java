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

        List<DiscordField> embedFields = new ArrayList<>();
        if (fields.getAssignee() != null) {
            String assigneeName = userMappingRepository.findName(fields.getAssignee().getAccountId())
                .orElse(fields.getAssignee().getDisplayName());
            embedFields.add(DiscordField.builder()
                .name("담당자").value(assigneeName).inline(true).build());
        }
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
            .color(EmbedColor.ISSUE_CREATED)
            .url(issue.getWebUrl())
            .fields(embedFields.isEmpty() ? null : embedFields)
            .build();

        String content = fields.getAssignee() != null
            ? userMappingRepository.findDiscordId(fields.getAssignee().getAccountId())
                .map(id -> "<@" + id + "> 새로운 작업이 할당되었습니다.")
                .orElse(null)
            : null;

        return DiscordMessage.builder().content(content).embeds(List.of(embed)).build();
    }
}

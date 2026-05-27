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
public class AssigneeChangedHandler implements JiraEventHandler {

    private final UserMappingRepository userMappingRepository;

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

        String newAssigneeName = assigneeChange.getTo() != null
            ? userMappingRepository.findName(assigneeChange.getTo()).orElse(assigneeChange.getToValue())
            : "없음";

        List<DiscordField> embedFields = new ArrayList<>();
        String prevAssigneeName = assigneeChange.getFrom() != null
            ? userMappingRepository.findName(assigneeChange.getFrom()).orElse(assigneeChange.getFromString())
            : (assigneeChange.getFromString() != null ? assigneeChange.getFromString() : "없음");
        embedFields.add(DiscordField.builder()
            .name("이전 담당자")
            .value(prevAssigneeName)
            .inline(true).build());
        embedFields.add(DiscordField.builder()
            .name("새 담당자")
            .value(newAssigneeName)
            .inline(true).build());

        DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .color(EmbedColor.ASSIGNEE_CHANGED)
            .url(issue.getWebUrl())
            .fields(embedFields)
            .build();

        String content = assigneeChange.getTo() != null
            ? userMappingRepository.findDiscordId(assigneeChange.getTo())
                .map(id -> "<@" + id + "> 새로운 작업이 할당되었습니다.")
                .orElse(null)
            : null;

        return DiscordMessage.builder().content(content).embeds(List.of(embed)).build();
    }
}

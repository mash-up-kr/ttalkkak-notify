package com.ttalkkak.notify.chat;

import com.ttalkkak.notify.notification.Mention;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.notification.NotificationField;
import com.ttalkkak.notify.user.UserInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMessageFormatter {

    public String format(NotificationEvent event) {
        List<String> sections = new ArrayList<>();

        String summary = buildSummary(event);
        if (summary != null) sections.add(summary);

        sections.add(event.title());

        if (event.fields() != null && !event.fields().isEmpty()) {
            String fieldsText = event.fields().stream()
                .map(f -> f.name() + ": " + f.value())
                .collect(Collectors.joining("\n"));
            sections.add(fieldsText);
        }

        if (event.description() != null && !event.description().isBlank()) {
            sections.add(event.description());
        }

        if (event.url() != null && !event.url().isBlank()) {
            sections.add(event.url());
        }

        return String.join("\n\n", sections);
    }

    private String buildSummary(NotificationEvent event) {
        return switch (event.eventType()) {
            case JIRA_ISSUE_CREATED -> buildAssignedSummary(event.mention());
            case JIRA_ASSIGNEE_CHANGED -> buildAssigneeChangedSummary(event.fields());
            case JIRA_COMMENT_CREATED, CONFLUENCE_COMMENT_CREATED -> buildMentionSummary(event.mention());
            default -> null;
        };
    }

    private String buildAssignedSummary(Mention mention) {
        if (mention == null || mention.targets().isEmpty()) return null;
        String name = mention.targets().get(0).getName();
        return name != null ? name + "님에게 새로운 작업이 할당되었습니다." : null;
    }

    private String buildAssigneeChangedSummary(List<NotificationField> fields) {
        if (fields == null) return null;
        String prev = findFieldValue(fields, "이전 담당자");
        String next = findFieldValue(fields, "새 담당자");
        if (prev == null || next == null) return null;
        return "담당자가 " + prev + "님에서 " + next + "님으로 변경되었습니다.";
    }

    private String buildMentionSummary(Mention mention) {
        if (mention == null || mention.targets().isEmpty()) return null;
        String names = mention.targets().stream()
            .map(UserInfo::getName)
            .filter(name -> name != null)
            .collect(Collectors.joining("님, "));
        if (names.isBlank()) return null;
        return mention.prefix() + names + "님을 언급했습니다.";
    }

    private String findFieldValue(List<NotificationField> fields, String name) {
        return fields.stream()
            .filter(f -> name.equals(f.name()))
            .map(NotificationField::value)
            .findFirst()
            .orElse(null);
    }
}

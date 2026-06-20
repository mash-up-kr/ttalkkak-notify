package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import com.ttalkkak.notify.notification.EventType;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.notification.NotificationField;
import com.ttalkkak.notify.user.UserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatusChangedHandler implements JiraEventHandler {

    private final UserMappingRepository userMappingRepository;

    @Override
    public boolean supports(JiraWebhookPayload payload) {
        if (!"jira:issue_updated".equals(payload.getWebhookEvent())) return false;
        if (payload.getChangelog() == null || payload.getChangelog().getItems() == null) return false;
        return payload.getChangelog().getItems().stream()
            .anyMatch(item -> "status".equals(item.getField()));
    }

    @Override
    public NotificationEvent handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Fields fields = issue.getFields();

        JiraWebhookPayload.ChangelogItem statusChange = payload.getChangelog().getItems().stream()
            .filter(item -> "status".equals(item.getField()))
            .findFirst().orElseThrow();

        List<NotificationField> notifFields = new ArrayList<>();
        notifFields.add(new NotificationField("상태 변경", statusChange.getFromString() + " → " + statusChange.getToValue(), false));
        if (fields.getAssignee() != null) {
            String name = userMappingRepository.findName(fields.getAssignee().getAccountId())
                .orElse(fields.getAssignee().getDisplayName());
            notifFields.add(new NotificationField("담당자", name, true));
        }
        if (fields.getPriority() != null) {
            notifFields.add(new NotificationField("우선순위", fields.getPriority().getName(), true));
        }

        return new NotificationEvent(
            EventType.JIRA_STATUS_CHANGED,
            "🔄 [" + issue.getKey() + "] " + fields.getSummary(),
            null,
            issue.getWebUrl(),
            notifFields,
            null
        );
    }
}

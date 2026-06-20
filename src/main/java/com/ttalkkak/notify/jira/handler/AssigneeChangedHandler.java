package com.ttalkkak.notify.jira.handler;

import com.ttalkkak.notify.jira.JiraEventHandler;
import com.ttalkkak.notify.jira.JiraWebhookPayload;
import com.ttalkkak.notify.notification.EventType;
import com.ttalkkak.notify.notification.Mention;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.notification.NotificationField;
import com.ttalkkak.notify.user.UserInfo;
import com.ttalkkak.notify.user.UserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    public NotificationEvent handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.ChangelogItem assigneeChange = payload.getChangelog().getItems().stream()
            .filter(item -> "assignee".equals(item.getField()))
            .findFirst().orElseThrow();

        String prevName = assigneeChange.getFrom() != null
            ? userMappingRepository.findName(assigneeChange.getFrom()).orElse(assigneeChange.getFromString())
            : (assigneeChange.getFromString() != null ? assigneeChange.getFromString() : "없음");
        String newName = assigneeChange.getTo() != null
            ? userMappingRepository.findName(assigneeChange.getTo()).orElse(assigneeChange.getToValue())
            : "없음";

        List<NotificationField> fields = List.of(
            new NotificationField("이전 담당자", prevName, true),
            new NotificationField("새 담당자", newName, true)
        );

        Mention mention = null;
        if (assigneeChange.getTo() != null) {
            UserInfo newAssignee = userMappingRepository.findUser(assigneeChange.getTo())
                .orElseGet(() -> new UserInfo(assigneeChange.getToValue(), assigneeChange.getTo(), null));
            mention = new Mention("", List.of(newAssignee), " 새로운 작업이 할당되었습니다.");
        }

        return new NotificationEvent(
            EventType.JIRA_ASSIGNEE_CHANGED,
            "👤 [" + issue.getKey() + "] " + issue.getFields().getSummary(),
            null,
            issue.getWebUrl(),
            fields,
            mention
        );
    }
}

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
    public NotificationEvent handle(JiraWebhookPayload payload) {
        JiraWebhookPayload.Issue issue = payload.getIssue();
        JiraWebhookPayload.Fields fields = issue.getFields();

        List<NotificationField> notifFields = new ArrayList<>();
        if (fields.getAssignee() != null) {
            String name = userMappingRepository.findName(fields.getAssignee().getAccountId())
                .orElse(fields.getAssignee().getDisplayName());
            notifFields.add(new NotificationField("담당자", name, true));
        }
        if (fields.getDecisionLevel() != null) {
            notifFields.add(new NotificationField("Decision Level", fields.getDecisionLevel().getValue(), true));
        }
        if (fields.getPriority() != null) {
            notifFields.add(new NotificationField("우선순위", fields.getPriority().getName(), true));
        }

        Mention mention = null;
        if (fields.getAssignee() != null) {
            UserInfo assignee = userMappingRepository.findUser(fields.getAssignee().getAccountId())
                .orElseGet(() -> new UserInfo(fields.getAssignee().getDisplayName(), fields.getAssignee().getAccountId(), null));
            mention = new Mention("", List.of(assignee), " 새로운 작업이 할당되었습니다.");
        }

        return new NotificationEvent(
            EventType.JIRA_ISSUE_CREATED,
            "🆕 [" + issue.getKey() + "] " + fields.getSummary(),
            null,
            issue.getWebUrl(),
            notifFields.isEmpty() ? null : notifFields,
            mention
        );
    }
}

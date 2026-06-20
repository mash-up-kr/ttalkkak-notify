package com.ttalkkak.notify.chat;

import com.ttalkkak.notify.notification.EventType;
import com.ttalkkak.notify.notification.Mention;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.notification.NotificationField;
import com.ttalkkak.notify.user.UserInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageFormatterTest {

    private final ChatMessageFormatter formatter = new ChatMessageFormatter();

    @Test
    void issueCreated_assigneeSummaryWithNimEge() {
        UserInfo assignee = new UserInfo("장민서", "acc-1", "discord-1");
        NotificationEvent event = new NotificationEvent(
            EventType.JIRA_ISSUE_CREATED,
            "🆕 [DDK-1] 제목",
            null,
            "https://ttalkkak.atlassian.net/browse/DDK-1",
            List.of(new NotificationField("담당자", "장민서", true)),
            new Mention("", List.of(assignee), " 새로운 작업이 할당되었습니다.")
        );

        assertThat(formatter.format(event)).isEqualTo(
            "장민서님에게 새로운 작업이 할당되었습니다.\n\n" +
            "🆕 [DDK-1] 제목\n\n" +
            "담당자: 장민서\n\n" +
            "https://ttalkkak.atlassian.net/browse/DDK-1"
        );
    }

    @Test
    void assigneeChanged_prevAndNextInSummary() {
        UserInfo newAssignee = new UserInfo("김철수", "acc-2", "discord-2");
        NotificationEvent event = new NotificationEvent(
            EventType.JIRA_ASSIGNEE_CHANGED,
            "👤 [DDK-1] 제목",
            null,
            "https://ttalkkak.atlassian.net/browse/DDK-1",
            List.of(
                new NotificationField("이전 담당자", "장민서", true),
                new NotificationField("새 담당자", "김철수", true)
            ),
            new Mention("", List.of(newAssignee), " 새로운 작업이 할당되었습니다.")
        );

        assertThat(formatter.format(event)).isEqualTo(
            "담당자가 장민서님에서 김철수님으로 변경되었습니다.\n\n" +
            "👤 [DDK-1] 제목\n\n" +
            "이전 담당자: 장민서\n새 담당자: 김철수\n\n" +
            "https://ttalkkak.atlassian.net/browse/DDK-1"
        );
    }

    @Test
    void commentMention_naturalKoreanSentence() {
        UserInfo mentioned = new UserInfo("김철수", "acc-2", null);
        NotificationEvent event = new NotificationEvent(
            EventType.JIRA_COMMENT_CREATED,
            "💬 [DDK-1] 제목",
            "장민서: 확인 부탁드립니다.",
            "https://ttalkkak.atlassian.net/browse/DDK-1",
            null,
            new Mention("장민서님이 댓글에서 ", List.of(mentioned), " 님을 언급했습니다.")
        );

        assertThat(formatter.format(event)).isEqualTo(
            "장민서님이 댓글에서 김철수님을 언급했습니다.\n\n" +
            "💬 [DDK-1] 제목\n\n" +
            "장민서: 확인 부탁드립니다.\n\n" +
            "https://ttalkkak.atlassian.net/browse/DDK-1"
        );
    }

    @Test
    void pageCreated_authorShownInFields() {
        NotificationEvent event = new NotificationEvent(
            EventType.CONFLUENCE_PAGE_CREATED,
            "📄 새 문서: 페이지",
            null,
            "https://example.com/page",
            List.of(new NotificationField("작성자", "장민서", true)),
            null
        );

        assertThat(formatter.format(event)).isEqualTo(
            "📄 새 문서: 페이지\n\n" +
            "작성자: 장민서\n\n" +
            "https://example.com/page"
        );
    }

    @Test
    void pageUpdated_editorShownInFields() {
        NotificationEvent event = new NotificationEvent(
            EventType.CONFLUENCE_PAGE_UPDATED,
            "✏️ 문서 수정: 페이지",
            null,
            "https://example.com/page",
            List.of(new NotificationField("수정자", "김철수", true)),
            null
        );

        assertThat(formatter.format(event)).isEqualTo(
            "✏️ 문서 수정: 페이지\n\n" +
            "수정자: 김철수\n\n" +
            "https://example.com/page"
        );
    }

    @Test
    void noMentionNoFields_onlyTitle() {
        NotificationEvent event = new NotificationEvent(
            EventType.CONFLUENCE_PAGE_CREATED,
            "📄 새 문서: 페이지",
            null,
            null,
            null,
            null
        );

        assertThat(formatter.format(event)).isEqualTo("📄 새 문서: 페이지");
    }
}

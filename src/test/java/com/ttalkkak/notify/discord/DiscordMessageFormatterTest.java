package com.ttalkkak.notify.discord;

import com.ttalkkak.notify.discord.model.DiscordEmbed;
import com.ttalkkak.notify.discord.model.DiscordMessage;
import com.ttalkkak.notify.discord.model.EmbedColor;
import com.ttalkkak.notify.notification.EventType;
import com.ttalkkak.notify.notification.Mention;
import com.ttalkkak.notify.notification.NotificationEvent;
import com.ttalkkak.notify.notification.NotificationField;
import com.ttalkkak.notify.user.UserInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordMessageFormatterTest {

    private final DiscordMessageFormatter formatter = new DiscordMessageFormatter();

    @Test
    void formatsEmbedWithTitleUrlColorAndFields() {
        NotificationEvent event = new NotificationEvent(
            EventType.JIRA_ISSUE_CREATED,
            "🆕 [DDK-1] 제목",
            null,
            "https://ttalkkak.atlassian.net/browse/DDK-1",
            List.of(
                new NotificationField("담당자", "장민서", true),
                new NotificationField("우선순위", "Medium", true)
            ),
            null
        );

        DiscordMessage message = formatter.format(event);

        assertThat(message.getContent()).isNull();
        assertThat(message.getEmbeds()).hasSize(1);

        DiscordEmbed embed = message.getEmbeds().get(0);
        assertThat(embed.getTitle()).isEqualTo("🆕 [DDK-1] 제목");
        assertThat(embed.getUrl()).isEqualTo("https://ttalkkak.atlassian.net/browse/DDK-1");
        assertThat(embed.getColor()).isEqualTo(EmbedColor.ISSUE_CREATED);
        assertThat(embed.getDescription()).isNull();
        assertThat(embed.getFields()).hasSize(2);
        assertThat(embed.getFields().get(0).getName()).isEqualTo("담당자");
        assertThat(embed.getFields().get(0).isInline()).isTrue();
    }

    @Test
    void buildsContentPingForAssigneeMention() {
        UserInfo assignee = new UserInfo("장민서", "acc-1", "discord-123");
        NotificationEvent event = new NotificationEvent(
            EventType.JIRA_ASSIGNEE_CHANGED,
            "👤 [DDK-1] 제목",
            null,
            null,
            null,
            new Mention("", List.of(assignee), " 새로운 작업이 할당되었습니다.")
        );

        DiscordMessage message = formatter.format(event);

        assertThat(message.getContent()).isEqualTo("<@discord-123> 새로운 작업이 할당되었습니다.");
    }

    @Test
    void buildsContentPingForCommentMentionWithMultipleUsers() {
        UserInfo user1 = new UserInfo("김철수", "acc-2", "discord-456");
        UserInfo user2 = new UserInfo("이영희", "acc-3", "discord-789");
        NotificationEvent event = new NotificationEvent(
            EventType.JIRA_COMMENT_CREATED,
            "💬 [DDK-1] 제목",
            "장민서: 확인 부탁드립니다.",
            null,
            null,
            new Mention("장민서님이 댓글에서 ", List.of(user1, user2), " 님을 언급했습니다.")
        );

        DiscordMessage message = formatter.format(event);

        assertThat(message.getContent())
            .isEqualTo("장민서님이 댓글에서 <@discord-456>, <@discord-789> 님을 언급했습니다.");
    }

    @Test
    void contentIsNullWhenMentionTargetHasNoDiscordId() {
        UserInfo unmapped = new UserInfo("외부인", "acc-unknown", null);
        NotificationEvent event = new NotificationEvent(
            EventType.JIRA_ISSUE_CREATED,
            "🆕 [DDK-1] 제목",
            null,
            null,
            null,
            new Mention("", List.of(unmapped), " 새로운 작업이 할당되었습니다.")
        );

        DiscordMessage message = formatter.format(event);

        assertThat(message.getContent()).isNull();
    }

    @Test
    void contentIsNullWhenNoMention() {
        NotificationEvent event = new NotificationEvent(
            EventType.CONFLUENCE_PAGE_CREATED,
            "📄 새 문서: 페이지",
            null,
            "https://example.com",
            null,
            null
        );

        DiscordMessage message = formatter.format(event);

        assertThat(message.getContent()).isNull();
        assertThat(message.getEmbeds().get(0).getColor()).isEqualTo(EmbedColor.CONFLUENCE_PAGE_CREATED);
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

        DiscordEmbed embed = formatter.format(event).getEmbeds().get(0);

        assertThat(embed.getTitle()).isEqualTo("📄 새 문서: 페이지");
        assertThat(embed.getColor()).isEqualTo(EmbedColor.CONFLUENCE_PAGE_CREATED);
        assertThat(embed.getFields()).hasSize(1);
        assertThat(embed.getFields().get(0).getName()).isEqualTo("작성자");
        assertThat(embed.getFields().get(0).getValue()).isEqualTo("장민서");
        assertThat(embed.getFields().get(0).isInline()).isTrue();
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

        DiscordEmbed embed = formatter.format(event).getEmbeds().get(0);

        assertThat(embed.getTitle()).isEqualTo("✏️ 문서 수정: 페이지");
        assertThat(embed.getColor()).isEqualTo(EmbedColor.CONFLUENCE_PAGE_UPDATED);
        assertThat(embed.getFields()).hasSize(1);
        assertThat(embed.getFields().get(0).getName()).isEqualTo("수정자");
    }

    @Test
    void descriptionIncludedWhenPresent() {
        NotificationEvent event = new NotificationEvent(
            EventType.CONFLUENCE_COMMENT_CREATED,
            "💬 댓글: 페이지",
            "장민서: 내용입니다.",
            null,
            null,
            null
        );

        DiscordEmbed embed = formatter.format(event).getEmbeds().get(0);

        assertThat(embed.getDescription()).isEqualTo("장민서: 내용입니다.");
        assertThat(embed.getColor()).isEqualTo(EmbedColor.CONFLUENCE_COMMENT);
    }
}

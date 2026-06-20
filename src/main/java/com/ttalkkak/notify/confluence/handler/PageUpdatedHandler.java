package com.ttalkkak.notify.confluence.handler;

import com.ttalkkak.notify.confluence.ConfluenceEventHandler;
import com.ttalkkak.notify.confluence.ConfluenceWebhookPayload;
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
public class PageUpdatedHandler implements ConfluenceEventHandler {

    private final UserMappingRepository userMappingRepository;

    @Override
    public boolean supports(ConfluenceWebhookPayload payload) {
        return "page_updated".equals(payload.getEvent());
    }

    @Override
    public NotificationEvent handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Page page = payload.getPage();

        List<NotificationField> fields = new ArrayList<>();
        userMappingRepository.findName(payload.getUserAccountId()).ifPresent(name ->
            fields.add(new NotificationField("수정자", name, true))
        );

        return new NotificationEvent(
            EventType.CONFLUENCE_PAGE_UPDATED,
            "✏️ 문서 수정: " + page.getTitle(),
            null,
            page.getSelf(),
            fields.isEmpty() ? null : fields,
            null
        );
    }
}

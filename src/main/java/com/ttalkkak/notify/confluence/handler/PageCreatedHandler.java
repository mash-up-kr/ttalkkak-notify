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
public class PageCreatedHandler implements ConfluenceEventHandler {

    private final UserMappingRepository userMappingRepository;

    @Override
    public boolean supports(ConfluenceWebhookPayload payload) {
        return "page_created".equals(payload.getEvent());
    }

    @Override
    public NotificationEvent handle(ConfluenceWebhookPayload payload) {
        ConfluenceWebhookPayload.Page page = payload.getPage();

        List<NotificationField> fields = new ArrayList<>();
        userMappingRepository.findName(payload.getUserAccountId()).ifPresent(name ->
            fields.add(new NotificationField("작성자", name, true))
        );

        return new NotificationEvent(
            EventType.CONFLUENCE_PAGE_CREATED,
            "📄 새 문서: " + page.getTitle(),
            null,
            page.getSelf(),
            fields.isEmpty() ? null : fields,
            null
        );
    }
}

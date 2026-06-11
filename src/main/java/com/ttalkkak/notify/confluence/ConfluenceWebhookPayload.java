package com.ttalkkak.notify.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceWebhookPayload {

    private String userAccountId;
    private String updateTrigger;
    private Long timestamp;
    private Page page;
    private Comment comment;

    public String getEvent() {
        if (comment != null) return "comment_created";
        if (page != null && "edit_page".equals(updateTrigger)) return "page_updated";
        if (page != null) return "page_created";
        return null;
    }

    public String getEventKey() {
        String event = getEvent();
        if (event == null || timestamp == null) {
            return null;
        }
        String identifier = comment != null ? comment.getId()
                : page != null ? page.getId()
                : null;
        if (identifier == null) {
            return null;
        }
        return event + ":" + identifier + ":" + timestamp;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page {
        private String id;
        private String title;
        private String spaceKey;
        private String self;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String id;
        private String self;
        private Page parent;
    }
}

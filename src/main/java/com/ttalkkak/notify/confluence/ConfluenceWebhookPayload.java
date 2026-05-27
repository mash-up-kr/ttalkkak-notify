package com.ttalkkak.notify.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceWebhookPayload {

    private String userAccountId;
    private String updateTrigger;
    private Page page;
    private Comment comment;

    public String getEvent() {
        if (comment != null) return "comment_created";
        if (page != null && "edit_page".equals(updateTrigger)) return "page_updated";
        if (page != null) return "page_created";
        return null;
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

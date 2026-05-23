package com.ttalkkak.notify.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceWebhookPayload {

    private String event;
    private Page page;
    private Comment comment;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page {
        private String id;
        private String title;
        private Space space;
        @JsonProperty("_links")
        private Links links;
        private Version version;

        public String getWebUrl() {
            if (links == null || links.getWebui() == null) return null;
            return "https://ttalkkak.atlassian.net/wiki" + links.getWebui();
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Space {
        private String key;
        private String name;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private String webui;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version {
        private User by;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String accountId;
        private String displayName;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String body;
        private User author;
        private Page page;
    }
}

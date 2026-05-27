package com.ttalkkak.notify.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraWebhookPayload {

    private String webhookEvent;
    private Issue issue;
    private Comment comment;
    private Changelog changelog;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {
        private String key;
        private Fields fields;

        public String getWebUrl() {
            return "https://ttalkkak.atlassian.net/browse/" + key;
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String summary;
        private Status status;
        private User assignee;
        private Priority priority;
        @JsonProperty("customfield_10147")
        private DecisionLevel decisionLevel;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DecisionLevel {
        private String value;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String name;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String accountId;
        private String displayName;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Priority {
        private String name;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String body;
        private User author;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Changelog {
        private List<ChangelogItem> items;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChangelogItem {
        private String field;
        private String from;
        private String fromString;
        @JsonProperty("toString")
        private String toValue;
        private String to;
    }
}

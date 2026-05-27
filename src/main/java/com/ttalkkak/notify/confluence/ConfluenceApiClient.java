package com.ttalkkak.notify.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Component
@Slf4j
public class ConfluenceApiClient {

    private static final String BASE_URL = "https://ttalkkak.atlassian.net";
    private static final int MAX_BODY_LENGTH = 200;

    private final RestClient restClient;

    public ConfluenceApiClient(ConfluenceApiProperties properties) {
        String credentials = Base64.getEncoder().encodeToString(
            (properties.email() + ":" + properties.token()).getBytes()
        );
        this.restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader("Authorization", "Basic " + credentials)
            .build();
    }

    public String fetchCommentBody(String commentId) {
        try {
            CommentResponse response = restClient.get()
                .uri("/wiki/rest/api/content/{id}?expand=body.storage", commentId)
                .retrieve()
                .body(CommentResponse.class);

            if (response == null || response.getBody() == null) return null;
            String value = response.getBody().getStorage().getValue();
            if (value == null) return null;

            String text = value.replaceAll("<[^>]+>", "").trim();
            return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "..." : text;
        } catch (Exception e) {
            log.warn("Confluence 댓글 본문 조회 실패 [commentId={}]: {}", commentId, e.getMessage());
            return null;
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class CommentResponse {
        private Body body;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Body {
            private Storage storage;

            @Getter
            @JsonIgnoreProperties(ignoreUnknown = true)
            static class Storage {
                private String value;
            }
        }
    }
}

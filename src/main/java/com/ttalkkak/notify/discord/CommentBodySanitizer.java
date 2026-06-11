package com.ttalkkak.notify.discord;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Jira wiki markup / Confluence storage HTML 댓글 본문을 Discord 임베드 description에 표시할
 * 평문(서식 제거, 줄바꿈은 공백으로 치환)으로 정규화한다.
 */
@UtilityClass
public class CommentBodySanitizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public String fromJiraWikiMarkup(String body) {
        String text = body;
        text = text.replaceAll("(?m)^h[1-6]\\.\\s*", "");
        text = text.replaceAll("(?m)^[#*\\-]+\\s+", "");
        text = text.replaceAll("\\{\\{([^}]*)\\}\\}", "$1");
        text = text.replaceAll("\\{[^}]*\\}", "");
        text = text.replaceAll("\\*([^\\s*][^*\\n]*?)\\*", "$1");
        text = text.replaceAll("_([^\\s_][^_\\n]*?)_", "$1");
        text = text.replaceAll("\\[([^|\\]]+)\\|[^\\]]+\\]", "$1");
        text = text.replaceAll("\\[([^\\]]+)\\]", "$1");
        return collapseWhitespace(text);
    }

    public String fromConfluenceStorageHtml(String html) {
        String text = html.replaceAll("(?i)</(p|li|h[1-6]|div|tr)>|<br\\s*/?>", " ");
        text = text.replaceAll("<[^>]+>", "");
        text = text.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"");
        return collapseWhitespace(text);
    }

    private String collapseWhitespace(String text) {
        return WHITESPACE.matcher(text).replaceAll(" ").trim();
    }
}

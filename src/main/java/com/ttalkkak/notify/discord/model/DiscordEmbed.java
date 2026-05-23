package com.ttalkkak.notify.discord.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscordEmbed {
    private String title;
    private String description;
    private int color;
    private String url;
    private List<DiscordField> fields;
}

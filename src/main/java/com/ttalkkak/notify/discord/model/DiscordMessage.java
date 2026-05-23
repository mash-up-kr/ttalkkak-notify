package com.ttalkkak.notify.discord.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscordMessage {
    private String username;
    private String content;
    private List<DiscordEmbed> embeds;
}

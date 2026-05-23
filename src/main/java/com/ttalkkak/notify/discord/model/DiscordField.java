package com.ttalkkak.notify.discord.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscordField {
    private String name;
    private String value;
    @Builder.Default
    private boolean inline = false;
}

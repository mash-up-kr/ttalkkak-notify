package com.ttalkkak.notify.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserInfo {
    private final String name;
    private final String jiraAccountId;
    private final String discordId;
}

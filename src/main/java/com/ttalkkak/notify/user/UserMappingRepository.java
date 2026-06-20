package com.ttalkkak.notify.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMappingRepository {

    private final UserMappingLoader loader;

    public Optional<String> findDiscordId(String jiraAccountId) {
        if (jiraAccountId == null) return Optional.empty();
        return loader.getUsers().stream()
            .filter(u -> jiraAccountId.equals(u.getJiraAccountId()))
            .map(UserInfo::getDiscordId)
            .findFirst();
    }

    public Optional<String> findName(String jiraAccountId) {
        if (jiraAccountId == null) return Optional.empty();
        return loader.getUsers().stream()
            .filter(u -> jiraAccountId.equals(u.getJiraAccountId()))
            .map(UserInfo::getName)
            .findFirst();
    }

    public Optional<UserInfo> findUser(String jiraAccountId) {
        if (jiraAccountId == null) return Optional.empty();
        return loader.getUsers().stream()
            .filter(u -> jiraAccountId.equals(u.getJiraAccountId()))
            .findFirst();
    }
}

package com.ttalkkak.notify.user;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserMappingLoader {

    private List<UserInfo> users = List.of();

    @PostConstruct
    public void load() {
        Path path = Path.of("config/user_mapping.yml");
        if (!Files.exists(path)) {
            log.warn("user_mapping.yml not found at {}", path.toAbsolutePath());
            return;
        }
        try (InputStream is = Files.newInputStream(path)) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(is);
            @SuppressWarnings("unchecked")
            List<Map<String, String>> rawUsers = (List<Map<String, String>>) root.get("users");
            if (rawUsers != null) {
                users = rawUsers.stream()
                    .map(m -> new UserInfo(m.get("name"), m.get("jira_account_id"), m.get("discord_id")))
                    .toList();
                log.info("Loaded {} user mappings", users.size());
            }
        } catch (IOException e) {
            log.error("Failed to load user_mapping.yml", e);
        }
    }

    public List<UserInfo> getUsers() {
        return users;
    }
}

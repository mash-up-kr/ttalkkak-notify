package com.ttalkkak.notify.notification;

import com.ttalkkak.notify.user.UserInfo;

import java.util.List;

public record Mention(
        String prefix,
        List<UserInfo> targets,
        String suffix
) {}

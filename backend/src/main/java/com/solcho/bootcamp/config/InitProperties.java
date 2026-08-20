package com.solcho.bootcamp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** app.init.* — 초기 시드 계정 설정. */
@ConfigurationProperties(prefix = "app.init")
public record InitProperties(
        String adminLoginId,
        String adminPassword,
        String adminNickname,
        String guestLoginId,
        String guestPassword,
        String guestNickname
) {
}

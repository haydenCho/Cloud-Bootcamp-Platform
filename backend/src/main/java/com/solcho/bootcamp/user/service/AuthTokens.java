package com.solcho.bootcamp.user.service;

import com.solcho.bootcamp.user.entity.User;

/** 서비스 → 컨트롤러 내부 전달용. refreshToken 원문은 컨트롤러가 httpOnly 쿠키로만 내보낸다. */
public record AuthTokens(String accessToken, String refreshToken, User user) {
}

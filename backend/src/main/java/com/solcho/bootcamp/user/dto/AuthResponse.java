package com.solcho.bootcamp.user.dto;

import com.solcho.bootcamp.user.entity.User;

/**
 * 로그인/회원가입 응답. accessToken 은 body 로, refreshToken 은 httpOnly 쿠키로 별도 전달된다.
 */
public record AuthResponse(
        String accessToken,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, User user) {
        return new AuthResponse(accessToken, UserResponse.from(user));
    }
}

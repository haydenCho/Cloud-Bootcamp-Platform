package com.solcho.bootcamp.user.dto;

import jakarta.validation.constraints.Size;

/** 닉네임/프로필 이미지 수정. 변경할 필드만 채워 보낸다(null 은 미변경). */
public record UpdateProfileRequest(
        @Size(min = 1, max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname,

        @Size(max = 255, message = "이미지 경로가 너무 깁니다.")
        String profileImageUrl
) {
}
